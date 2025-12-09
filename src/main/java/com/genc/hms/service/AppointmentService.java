package com.genc.hms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.genc.hms.dto.AppointmentRequestDTO;
import com.genc.hms.dto.AppointmentResponseDTO;
import com.genc.hms.entity.Appointment;
import com.genc.hms.entity.Doctor;
import com.genc.hms.entity.DoctorAvailability;
import com.genc.hms.entity.Patient;
import com.genc.hms.enums.AppointmentStatus;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.repository.AppointmentRepository;

@Service
public class AppointmentService {

	// --- Constants ---
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final int SLOT_DURATION_MINUTES = 30; // Standard appointment duration

	// --- Dependencies ---
	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private PatientService patientService;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private BillingService billingService;

	// =================================================================================
	// I. PRIVATE UTILITY & VALIDATION METHODS (MODIFIED to throw clear exceptions)
	// =================================================================================

	/**
	 * Converts a time slot string ("HH:mm-HH:mm") into LocalTime start/end objects.
	 */
	private LocalTime[] parseTimeSlot(String timeSlot) {
		String[] parts = timeSlot.split("-");
		if (parts.length != 2)
			throw new IllegalArgumentException("Invalid time slot format: " + timeSlot);
		try {
			return new LocalTime[] { LocalTime.parse(parts[0], TIME_FORMATTER),
					LocalTime.parse(parts[1], TIME_FORMATTER) };
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid time format in slot: " + timeSlot);
		}
	}

	/**
	 * Validates a requested appointment slot against a doctor's general schedule
	 * and standard duration. Throws IllegalArgumentException on failure.
	 */
	private void validateTimeSlot(Doctor doctor, LocalDate date, String timeSlot) {
		LocalTime[] times = parseTimeSlot(timeSlot);
		LocalTime startTime = times[0];
		LocalTime endTime = times[1];

		long duration = ChronoUnit.MINUTES.between(startTime, endTime);
		if (duration != SLOT_DURATION_MINUTES) {
			throw new IllegalArgumentException(
					"Requested slot duration must be " + SLOT_DURATION_MINUTES + " minutes.");
		}

		// Check general availability
		DoctorAvailability availability = checkDoctorGeneralAvailability(doctor, date).orElseThrow(
				() -> new IllegalArgumentException("Doctor is not available on " + date.getDayOfWeek() + "."));

		// Check if slot falls within working hours
		if (startTime.isBefore(availability.getStartTime()) || endTime.isAfter(availability.getEndTime())) {
			throw new IllegalArgumentException("Requested slot is outside the doctor's scheduled working hours ("
					+ availability.getStartTime().format(TIME_FORMATTER) + " - "
					+ availability.getEndTime().format(TIME_FORMATTER) + ").");
		}
	}

	/**
	 * Returns the DoctorAvailability for the given date's weekday.
	 */
	private Optional<DoctorAvailability> checkDoctorGeneralAvailability(Doctor doctor, LocalDate date) {
		return doctor.getDoctorAvailabilities().stream()
				.filter(a -> a.getDayOfWeek().name().equals(date.getDayOfWeek().name())).findFirst();
	}

	/**
	 * Checks if the requested slot is already booked for a doctor (ignoring
	 * cancelled appointments). Throws IllegalStateException on conflict.
	 */
	private void checkSlotConflict(Long doctorId, LocalDate date, String timeSlot) {
		if (appointmentRepository.countByDoctorDoctorIdAndAppointmentDateAndTimeSlotAndStatusNot(doctorId, date,
				timeSlot, AppointmentStatus.CANCELLED) > 0) {
			throw new IllegalStateException("The requested time slot " + timeSlot + " is already booked for doctor "
					+ doctorId + " on " + date + ".");
		}
	}

	/**
	 * Validates that the requested appointment date and time is in the future.
	 * Throws IllegalArgumentException if the time is in the past.
	 */
	private void validateAppointmentTimeInFuture(LocalDate date, String timeSlot) {
		// Assuming timeSlot is "HH:mm-HH:mm"
		LocalTime[] times = parseTimeSlot(timeSlot);
		LocalTime startTime = times[0];

		// Combine date and start time to get the exact appointment moment
		LocalDateTime appointmentDateTime = LocalDateTime.of(date, startTime);
		LocalDateTime currentDateTime = LocalDateTime.now();

		if (appointmentDateTime.isBefore(currentDateTime)) {
			throw new IllegalArgumentException("Cannot book an appointment in the past. Requested time: "
					+ appointmentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
		}
	}

	/**
	 * Maps an Appointment entity to a response DTO.
	 */
	private AppointmentResponseDTO mapAppointmentToResponseDTO(Appointment appointment) {
		Doctor doctor = appointment.getDoctor();
		Patient patient = appointment.getPatient();
		return new AppointmentResponseDTO(appointment.getAppointmentId(), patient.getPatientId(), patient.getName(),
				doctor.getDoctorId(), doctor.getName(), doctor.getSpecialization(), appointment.getAppointmentDate(),
				appointment.getTimeSlot(), appointment.getReason(), appointment.getStatus(), appointment.getRemarks());
	}

	/**
	 * * Retrieves an AppointmentResponseDTO by ID, throws if not found. Used by
	 * controllers to simplify retrieval.
	 */
	@Transactional(readOnly = true)
	public AppointmentResponseDTO getAppointmentResponseById(Long appointmentId) {
		return appointmentRepository.findById(appointmentId).map(this::mapAppointmentToResponseDTO)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
	}

	// =================================================================================
	// II. READ OPERATIONS (GET) (Kept the original methods, adding exception
	// behavior to getAvailableSlots)
	// =================================================================================

	/** Returns all appointments (admin view). */
	@Transactional(readOnly = true)
	public List<AppointmentResponseDTO> findAllAppointments() {
		return appointmentRepository.findAll().stream().map(this::mapAppointmentToResponseDTO).toList();
	}

	/** Returns all appointments for a specific patient. */
	@Transactional(readOnly = true)
	public List<AppointmentResponseDTO> getAppointmentsForPatient(Long patientId) {
		return appointmentRepository.findByPatient_PatientIdOrderByAppointmentDateAsc(patientId).stream()
				.map(this::mapAppointmentToResponseDTO).toList();
	}

	/** Returns all appointments for a specific doctor. */
	@Transactional(readOnly = true)
	public List<AppointmentResponseDTO> getAppointmentsForDoctor(Long doctorId) {
		return appointmentRepository.findByDoctor_DoctorIdOrderByAppointmentDateAsc(doctorId).stream()
				.map(this::mapAppointmentToResponseDTO).toList();
	}

	/** Finds an appointment by ID and returns the DTO. */
	@Transactional(readOnly = true)
	public Optional<AppointmentResponseDTO> findAppointmentById(Long appointmentId) {
		return appointmentRepository.findById(appointmentId).map(this::mapAppointmentToResponseDTO);
	}

	/** Returns the raw Appointment entity (internal use). */
	@Transactional(readOnly = true)
	public Optional<Appointment> findById(Long appointmentId) {
		return appointmentRepository.findById(appointmentId);
	}

	/**
	 * Returns all available 30-minute slots for a doctor on a given date. Throws
	 * ResourceNotFoundException if doctorId is invalid.
	 */
	@Transactional(readOnly = true)
	public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
		// Use orElseThrow pattern
		Doctor doctor = doctorService.findById(doctorId)
				.orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

		Optional<DoctorAvailability> availabilityOpt = checkDoctorGeneralAvailability(doctor, date);
		if (availabilityOpt.isEmpty())
			return List.of(); // Returns empty if no general availability

		DoctorAvailability availability = availabilityOpt.get();
		List<String> allSlots = new ArrayList<>();
		LocalTime current = availability.getStartTime();

		while (current.plusMinutes(SLOT_DURATION_MINUTES).isBefore(availability.getEndTime().plusSeconds(1))) {
			String slot = current.format(TIME_FORMATTER) + "-"
					+ current.plusMinutes(SLOT_DURATION_MINUTES).format(TIME_FORMATTER);
			allSlots.add(slot);
			current = current.plusMinutes(SLOT_DURATION_MINUTES);
		}

		// Remove already booked slots
		Set<String> booked = appointmentRepository
				.findByDoctorDoctorIdAndAppointmentDateAndStatusNot(doctorId, date, AppointmentStatus.CANCELLED)
				.stream().map(Appointment::getTimeSlot).collect(Collectors.toSet());

		return allSlots.stream().filter(s -> !booked.contains(s)).toList();
	}

	// =================================================================================
	// III. CREATE OPERATION (Book) (MODIFIED to throw exceptions)
	// =================================================================================

	/**
	 * * Books a new appointment with validation against availability and conflicts.
	 * Throws ResourceNotFoundException, IllegalArgumentException, or
	 * IllegalStateException on failure.
	 */
	@Transactional
	public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO requestDTO) {

		// 1. New Check: Validate if the appointment time is in the past
		validateAppointmentTimeInFuture(requestDTO.getAppointmentDate(), requestDTO.getTimeSlot());

		// Retrieve entities or throw 404
		Patient patient = patientService.findById(requestDTO.getPatientId()).orElseThrow(
				() -> new ResourceNotFoundException("Patient not found with ID: " + requestDTO.getPatientId()));
		Doctor doctor = doctorService.findById(requestDTO.getDoctorId()).orElseThrow(
				() -> new ResourceNotFoundException("Doctor not found with ID: " + requestDTO.getDoctorId()));

		// Validate slot against doctor's schedule (throws IllegalArgumentException)
		validateTimeSlot(doctor, requestDTO.getAppointmentDate(), requestDTO.getTimeSlot());

		// Validate slot against existing bookings (throws IllegalStateException)
		checkSlotConflict(requestDTO.getDoctorId(), requestDTO.getAppointmentDate(), requestDTO.getTimeSlot());

		Appointment appointment = new Appointment();
		appointment.setPatient(patient);
		appointment.setDoctor(doctor);
		appointment.setAppointmentDate(requestDTO.getAppointmentDate());
		appointment.setTimeSlot(requestDTO.getTimeSlot());
		appointment.setReason(requestDTO.getReason());
		appointment.setStatus(AppointmentStatus.CONFIRMED);

		Appointment saved = appointmentRepository.save(appointment);

		// Billing service should now return DTO or throw exception (we don't need the
		// result here)
		billingService.createInitialBill(saved, doctor);

		return mapAppointmentToResponseDTO(saved);
	}

	// =================================================================================
	// IV. UPDATE & CANCEL OPERATIONS (MODIFIED to throw exceptions)
	// =================================================================================

	/**
	 * * Reschedules an appointment to a new date/time/doctor with validations.
	 * Throws ResourceNotFoundException or IllegalStateException on failure.
	 */
	@Transactional
	public AppointmentResponseDTO rescheduleAppointment(Long appointmentId, AppointmentRequestDTO requestDTO) {

		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

		if (appointment.getStatus() == AppointmentStatus.CANCELLED
				|| appointment.getStatus() == AppointmentStatus.COMPLETED) {
			throw new IllegalStateException(
					"Appointment cannot be rescheduled in " + appointment.getStatus().name() + " status.");
		}

		Doctor newDoctor = doctorService.findById(requestDTO.getDoctorId()).orElseThrow(
				() -> new ResourceNotFoundException("New doctor not found with ID: " + requestDTO.getDoctorId()));

		LocalDate newDate = requestDTO.getAppointmentDate();
		String newTime = requestDTO.getTimeSlot();

		// 🚨 NEW CHECK: Validate if the new appointment time is in the past
		validateAppointmentTimeInFuture(newDate, newTime);

		// Validate against availability (throws IllegalArgumentException)
		validateTimeSlot(newDoctor, newDate, newTime);

		boolean unchanged = appointment.getAppointmentDate().equals(newDate)
				&& appointment.getTimeSlot().equals(newTime)
				&& appointment.getDoctor().getDoctorId().equals(newDoctor.getDoctorId());

		// Check conflict only if there's a change that could cause one
		if (!unchanged) {
			checkSlotConflict(newDoctor.getDoctorId(), newDate, newTime);
		}

		// Update doctor and trigger billing if doctor changed
		if (!appointment.getDoctor().getDoctorId().equals(newDoctor.getDoctorId())) {
			appointment.setDoctor(newDoctor);
			// Service method now throws exceptions
			billingService.updateBillForDoctorChange(appointment, newDoctor);
		}

		// Update details
		appointment.setAppointmentDate(newDate);
		appointment.setTimeSlot(newTime);
		appointment.setReason(requestDTO.getReason());
		appointment.setStatus(AppointmentStatus.CONFIRMED); // Reset status on successful reschedule

		return mapAppointmentToResponseDTO(appointmentRepository.save(appointment));
	}

	/**
	 * * Updates appointment status (e.g., COMPLETED, CANCELLED). Throws
	 * ResourceNotFoundException if appointment is missing.
	 */
	@Transactional
	public AppointmentResponseDTO updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus,
			String remarks) { // 🚨 Direct DTO return

		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

		// You might add checks here, e.g., if (newStatus == AppointmentStatus.CANCELLED
		// && appointment.getStatus() == AppointmentStatus.COMPLETED) throw new
		// IllegalStateException(...)

		appointment.setStatus(newStatus);
		appointment.setRemarks(remarks);

		// If the new status is CANCELLED, trigger billing logic
		if (newStatus == AppointmentStatus.CANCELLED) {
			// Service method now throws exceptions, but we use it for side effect
			try {
				billingService.cancelBillForAppointment(appointment);
			} catch (ResourceNotFoundException e) {
				// Log and continue if bill is missing, as the primary action is to cancel the
				// appointment
				// logger.warn("Billing record missing for cancelled appointment {}: {}",
				// appointmentId, e.getMessage());
			}
		}

		return mapAppointmentToResponseDTO(appointmentRepository.save(appointment));
	}

	/**
	 * * Cancels an appointment and triggers corresponding billing cancellation.
	 * Throws ResourceNotFoundException or IllegalStateException on failure.
	 */
	@Transactional
	public void cancelAppointment(Long appointmentId) { // 🚨 Void return

		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

		if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
			throw new IllegalStateException("Completed appointments cannot be cancelled.");
		}

		if (appointment.getStatus() != AppointmentStatus.CANCELLED) {
			appointment.setStatus(AppointmentStatus.CANCELLED);
			appointmentRepository.save(appointment);

			// Trigger billing service
			try {
				billingService.cancelBillForAppointment(appointment);
			} catch (ResourceNotFoundException e) {
				// Log and continue if bill is missing
				// logger.warn("Billing record missing for cancelled appointment {}: {}",
				// appointmentId, e.getMessage());
			}
		}
		// If it was already CANCELLED, we just return void (idempotent action)
	}

	public Long getCount() {
		return appointmentRepository.count();
	}
}