package com.genc.hms.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.genc.hms.dto.AppointmentRequestDTO;
import com.genc.hms.dto.AppointmentResponseDTO;
import com.genc.hms.enums.AppointmentStatus;
import com.genc.hms.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping("/api/appointments")
public class AppointmentController {

	private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

	// 🚨 Use constructor injection
	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	// ----------------- Creation & Slots -----------------

	/**
	 * POST /api/appointments Service throws ResourceNotFoundException (404),
	 * IllegalArgumentException/IllegalStateException (400) on failure.
	 */
	@PostMapping
	public ResponseEntity<AppointmentResponseDTO> bookAppointment(
			@Valid @RequestBody AppointmentRequestDTO requestDTO) {
		logger.info("Booking appointment for patient {} with doctor {} on {}", requestDTO.getPatientId(),
				requestDTO.getDoctorId(), requestDTO.getAppointmentDate());

		// Service returns DTO directly or throws exception
		AppointmentResponseDTO dto = appointmentService.bookAppointment(requestDTO);

		return new ResponseEntity<>(dto, HttpStatus.CREATED);
	}

	/**
	 * GET /api/appointments/slots/{doctorId}?date={date} Service throws
	 * ResourceNotFoundException (404) if doctorId is invalid.
	 */
	@GetMapping("/slots/{doctorId}")
	public ResponseEntity<List<String>> getAvailableSlots(@PathVariable Long doctorId, @RequestParam LocalDate date) {
		logger.info("Fetching available slots for doctor {} on {}", doctorId, date);

		// Service throws exception if doctorId is invalid
		List<String> slots = appointmentService.getAvailableSlots(doctorId, date);

		return ResponseEntity.ok(slots);
	}

	// ----------------- Retrieval -----------------

	/**
	 * GET /api/appointments/all NOTE: This endpoint should be secured for
	 * ADMIN/DOCTOR access in SecurityConfig.
	 */
	@GetMapping("/all")
	public ResponseEntity<List<AppointmentResponseDTO>> findAllAppointments() {
		logger.info("Retrieving all appointments (Admin view)");
		return ResponseEntity.ok(appointmentService.findAllAppointments());
	}

	/**
	 * GET /api/appointments/{appointmentId} Service throws
	 * ResourceNotFoundException (404) if ID is invalid. NOTE: Service layer must
	 * handle authorization (e.g., patient must match appointment patientId).
	 */
	@GetMapping("/{appointmentId}")
	public ResponseEntity<AppointmentResponseDTO> findAppointmentById(@PathVariable Long appointmentId) {
		logger.info("Fetching appointment {}", appointmentId);

		// Service throws ResourceNotFoundException (404)
		AppointmentResponseDTO dto = appointmentService.getAppointmentResponseById(appointmentId);

		return ResponseEntity.ok(dto);
	}

	/**
	 * GET /api/appointments/patient/{patientId} NOTE: Controller/Service layer must
	 * ensure the authenticated user matches {patientId}.
	 */
	@GetMapping("/patient/{patientId}")
	public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsForPatient(@PathVariable Long patientId) {
		logger.info("Fetching appointments for patient {}", patientId);

		// Assuming the service layer handles the potential ResourceNotFound for
		// patientId if necessary
		return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(patientId));
	}

	/**
	 * GET /api/appointments/doctor/{doctorId} NOTE: Controller/Service layer must
	 * ensure the authenticated user matches {doctorId}.
	 */
	@GetMapping("/doctor/{doctorId}")
	public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsForDoctor(@PathVariable Long doctorId) {
		logger.info("Fetching appointments for doctor {}", doctorId);

		// Assuming the service layer handles the potential ResourceNotFound for
		// doctorId if necessary
		return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctorId));
	}

	// ----------------- Modification -----------------

	/**
	 * PUT /api/appointments/{appointmentId}/reschedule Service throws
	 * ResourceNotFoundException (404),
	 * IllegalArgumentException/IllegalStateException (400) on failure.
	 */
	@PutMapping("/{appointmentId}/reschedule")
	public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(@PathVariable Long appointmentId,
			@Valid @RequestBody AppointmentRequestDTO requestDTO) {

		logger.info("Rescheduling appointment {} to {} for doctor {}", appointmentId, requestDTO.getAppointmentDate(),
				requestDTO.getDoctorId());

		// Service returns DTO or throws exception
		AppointmentResponseDTO dto = appointmentService.rescheduleAppointment(appointmentId, requestDTO);

		return ResponseEntity.ok(dto);
	}

	/**
	 * PUT /api/appointments/{appointmentId}/status Service throws
	 * ResourceNotFoundException (404) if ID is invalid.
	 */
	@PutMapping("/{appointmentId}/status")
	public ResponseEntity<AppointmentResponseDTO> updateAppointmentStatus(@PathVariable Long appointmentId,
			@RequestParam AppointmentStatus status, @RequestParam(required = false) String remarks) {

		logger.info("Updating appointment {} status to {}. Remarks: {}", appointmentId, status, remarks);

		// Service returns DTO or throws exception
		AppointmentResponseDTO dto = appointmentService.updateAppointmentStatus(appointmentId, status, remarks);

		return ResponseEntity.ok(dto);
	}

	/**
	 * PUT /api/appointments/{appointmentId}/cancel Service throws
	 * ResourceNotFoundException (404) or IllegalStateException (400) on failure.
	 */
	@PutMapping("/{appointmentId}/cancel")
	public ResponseEntity<Void> cancelAppointment(@PathVariable Long appointmentId) {
		logger.info("Cancelling appointment {}", appointmentId);

		// Service throws exception on failure, otherwise returns void (success)
		appointmentService.cancelAppointment(appointmentId);

		return ResponseEntity.noContent().build();
	}
}