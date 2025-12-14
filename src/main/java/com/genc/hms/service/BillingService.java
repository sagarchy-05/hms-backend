package com.genc.hms.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.genc.hms.dto.BillResponseDTO;
import com.genc.hms.entity.Appointment;
import com.genc.hms.entity.Bill;
import com.genc.hms.entity.Doctor;
import com.genc.hms.entity.User; // 🚨 For authorization methods
import com.genc.hms.enums.PaymentStatus;
import com.genc.hms.enums.Role;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.repository.BillRepository;

@Service
public class BillingService {

	// --- Dependencies ---
	@Autowired
	private BillRepository billRepository;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private UserService userService; // 🚨 Required for fetching the authenticated patient ID

	// =================================================================================
	// I. PRIVATE UTILITY METHODS
	// =================================================================================

	// ... (mapBillToResponseDTO method remains unchanged)

	private BillResponseDTO mapBillToResponseDTO(Bill bill) {
		return new BillResponseDTO(bill.getBillId(), bill.getPatient().getPatientId(), bill.getPatient().getName(),
				bill.getAppointment().getAppointmentId(), bill.getBillAmount(), bill.getPaymentStatus(),
				bill.getBillDate());
	}

	private BigDecimal getConsultationFee(Doctor doctor) {
		return doctorService.getDoctorFee(doctor);
	}

	// =================================================================================
	// II. CREATE & WRITE OPERATIONS (MODIFIED for error propagation)
	// =================================================================================

	/**
	 * Creates the initial bill for a newly booked appointment. Throws
	 * IllegalStateException if bill already exists (a business constraint).
	 */
	@Transactional
	public BillResponseDTO createInitialBill(Appointment appointment, Doctor doctor) { // 🚨 Direct DTO return
		if (billRepository.findByAppointment(appointment).isPresent()) {
			// Throw an exception for duplicate bill creation attempt
			throw new IllegalStateException(
					"Bill already exists for appointment ID: " + appointment.getAppointmentId());
		}

		Bill bill = new Bill();
		bill.setAppointment(appointment);
		bill.setPatient(appointment.getPatient());
		bill.setBillAmount(getConsultationFee(doctor));
		bill.setBillDate(LocalDate.now());
		bill.setPaymentStatus(PaymentStatus.PENDING);

		Bill savedBill = billRepository.save(bill);
		return mapBillToResponseDTO(savedBill); // 🚨 Direct DTO return
	}

	/**
	 * Records payment for a bill and marks it as PAID. Throws
	 * ResourceNotFoundException if bill ID is invalid.
	 */
	@Transactional
	public BillResponseDTO recordPayment(Long billId) { // 🚨 Direct DTO return

		Bill bill = billRepository.findById(billId)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

		if (bill.getPaymentStatus() == PaymentStatus.PAID) {
			return mapBillToResponseDTO(bill); // Already paid
		}

		bill.setPaymentStatus(PaymentStatus.PAID);
		Bill updatedBill = billRepository.save(bill);
		return mapBillToResponseDTO(updatedBill);
	}

	/**
	 * Updates the bill when the doctor associated with an appointment changes.
	 * Throws ResourceNotFoundException if bill is missing for the appointment.
	 */
	@Transactional
	public BillResponseDTO updateBillForDoctorChange(Appointment appointment, Doctor newDoctor) { // 🚨 Direct DTO
																									// return

		Bill bill = billRepository.findByAppointment(appointment).orElseThrow(() -> new ResourceNotFoundException(
				"Bill not found for appointment: " + appointment.getAppointmentId()));

		BigDecimal newFee = doctorService.getDoctorFee(newDoctor);
		bill.setBillAmount(newFee);
		Bill updatedBill = billRepository.save(bill);
		return mapBillToResponseDTO(updatedBill);
	}

	/**
	 * Handles billing when an appointment is cancelled. Throws
	 * ResourceNotFoundException if bill is missing for the appointment.
	 */
	@Transactional
	public BillResponseDTO cancelBillForAppointment(Appointment appointment) { // 🚨 Direct DTO return

		Bill bill = billRepository.findByAppointment(appointment).orElseThrow(() -> new ResourceNotFoundException(
				"Bill not found for appointment: " + appointment.getAppointmentId()));

		if (bill.getPaymentStatus() == PaymentStatus.PAID) {
			bill.setPaymentStatus(PaymentStatus.REFUNDED);
		} else if (bill.getPaymentStatus() == PaymentStatus.PENDING) {
			bill.setPaymentStatus(PaymentStatus.CANCELLED);
		}

		Bill updatedBill = billRepository.save(bill);
		return mapBillToResponseDTO(updatedBill);
	}

	// =================================================================================
	// IV. AUTHORIZATION & READ OPERATIONS (MODIFIED for security)
	// =================================================================================

	/**
	 * Retrieves a specific bill by its ID with patient authorization check. Used by
	 * the BillingController's GET /bills/{billId}.
	 */
	@Transactional(readOnly = true)
	public Optional<BillResponseDTO> getBillByIdAndAuthorize(Long billId, User currentUser) {
		return billRepository.findById(billId).flatMap(bill -> {
			if (isAuthorized(bill, currentUser)) {
				return Optional.of(mapBillToResponseDTO(bill));
			}
			return Optional.empty(); // Authorization failed
		});
	}

	/**
	 * Retrieves the bill by appointment ID with patient authorization check. Used
	 * by the BillingController's GET /bills/appointment/{appointmentId}.
	 */
	@Transactional(readOnly = true)
	public Optional<BillResponseDTO> getBillByAppointmentIdAndAuthorize(Long appointmentId, User currentUser) {
		return billRepository.findByAppointment_AppointmentId(appointmentId).flatMap(bill -> {
			if (isAuthorized(bill, currentUser)) {
				return Optional.of(mapBillToResponseDTO(bill));
			}
			return Optional.empty(); // Authorization failed
		});
	}

	/**
	 * Core authorization logic. Grants access if user is ADMIN/DOCTOR, or if the
	 * user is a PATIENT and the bill belongs to them.
	 */
	private boolean isAuthorized(Bill bill, User currentUser) {
		// ADMIN and DOCTOR always have full access
		if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.DOCTOR) {
			return true;
		}

		// PATIENT check
		if (currentUser.getRole() == Role.PATIENT) {
			Long authenticatedPatientId = userService.getRoleIdFromUser(currentUser);
			if (authenticatedPatientId != null && authenticatedPatientId.equals(bill.getPatient().getPatientId())) {
				return true;
			}
		}

		return false;
	}

	// --- Standard Read Operations ---

	/** Retrieves all bills in the system. */
	@Transactional(readOnly = true)
	public List<BillResponseDTO> getAllBills() {
		return billRepository.findAll().stream().map(this::mapBillToResponseDTO).toList();
	}

	/** Retrieves all bills for a specific patient, newest first. */
	@Transactional(readOnly = true)
	public List<BillResponseDTO> getBillsByPatient(Long patientId) {
		// Note: The controller handles the PATIENT authorization check for this
		// endpoint.
		return billRepository.findByPatientPatientIdOrderByBillDateDesc(patientId).stream()
				.map(this::mapBillToResponseDTO).toList();
	}

	/** Retrieves bills filtered by payment status. */
	@Transactional(readOnly = true)
	public List<BillResponseDTO> getBillsByStatus(PaymentStatus status) {
		return billRepository.findByPaymentStatus(status).stream().map(this::mapBillToResponseDTO).toList();
	}

	/** Returns the total count of bills in the system. */
	public Long getCount() {
		return billRepository.count();
	}
}