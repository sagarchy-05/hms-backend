package com.genc.hms.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // For explicit role/ID checks
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.genc.hms.dto.BillResponseDTO;
import com.genc.hms.entity.User;
import com.genc.hms.enums.PaymentStatus;
import com.genc.hms.enums.Role;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.service.BillingService;
import com.genc.hms.service.UserService; // Needed to get the role ID

@RestController
@CrossOrigin
@RequestMapping("/api/bills")
public class BillingController {

	private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

	// Use constructor injection
	private final BillingService billingService;
	private final UserService userService; // To retrieve the patient/doctor ID from the user entity

	public BillingController(BillingService billingService, UserService userService) {
		this.billingService = billingService;
		this.userService = userService;
	}

	/**
	 * Helper to enforce Patient can only view their own bills.
	 */
	private void enforcePatientAccess(User currentUser, Long patientIdToCheck) {
		if (currentUser.getRole() == Role.PATIENT) {
			Long authenticatedPatientId = userService.getRoleIdFromUser(currentUser);
			if (authenticatedPatientId == null || !authenticatedPatientId.equals(patientIdToCheck)) {
				logger.warn("Patient {} attempted to access unauthorized bill data for patient {}",
						authenticatedPatientId, patientIdToCheck);
				throw new AccessDeniedException("You are not authorized to view bills for this patient.");
			}
		}
	}

	// ----------------- Retrieval -----------------

	/**
	 * GET /api/bills/{billId} Retrieves a bill by ID. Patient access is authorized
	 * by the service layer which should verify the bill's patient ID matches the
	 * authenticated patient ID.
	 */
	@GetMapping("/{billId}")
	public ResponseEntity<BillResponseDTO> getBillById(@PathVariable Long billId,
			@AuthenticationPrincipal User currentUser) {

		logger.info("User {} fetching bill by ID {}", currentUser.getUserId(), billId);

		// The service layer must handle the authorization check against the bill's
		// patient ID
		BillResponseDTO dto = billingService.getBillByIdAndAuthorize(billId, currentUser).orElseThrow(() -> {
			logger.warn("Bill {} not found or access denied by service layer.", billId);
			return new ResourceNotFoundException("Bill not found with ID: " + billId);
		});

		logger.info("Bill {} found", billId);
		return ResponseEntity.ok(dto);
	}

	/**
	 * GET /api/bills/appointment/{appointmentId} Retrieves a bill using its related
	 * appointment ID.
	 */
	@GetMapping("/appointment/{appointmentId}")
	public ResponseEntity<BillResponseDTO> getBillByAppointmentId(@PathVariable Long appointmentId,
			@AuthenticationPrincipal User currentUser) {

		logger.info("User {} fetching bill for appointment {}", currentUser.getUserId(), appointmentId);

		// Service must handle the authorization check (via appointment's patient)
		BillResponseDTO dto = billingService.getBillByAppointmentIdAndAuthorize(appointmentId, currentUser)
				.orElseThrow(() -> {
					logger.warn("Bill for appointment {} not found or access denied.", appointmentId);
					return new ResourceNotFoundException("Bill not found for appointment ID: " + appointmentId);
				});

		logger.info("Bill for appointment {} found", appointmentId);
		return ResponseEntity.ok(dto);
	}

	/**
	 * GET /api/bills/patient/{patientId} Retrieves all bills for a specific
	 * patient. Security: ADMIN/DOCTOR can view any patient. PATIENT must match
	 * their own ID.
	 */
	@GetMapping("/patient/{patientId}")
	public ResponseEntity<List<BillResponseDTO>> getBillsForPatient(@PathVariable Long patientId,
			@AuthenticationPrincipal User currentUser) {

		logger.info("User {} fetching bills for patient {}", currentUser.getUserId(), patientId);

		// External Authorization Check (for PATIENT role)
		enforcePatientAccess(currentUser, patientId);

		List<BillResponseDTO> bills = billingService.getBillsByPatient(patientId);
		logger.info("Found {} bills for patient {}", bills.size(), patientId);
		return ResponseEntity.ok(bills);
	}

	/**
	 * GET /api/bills?status={status} Retrieves bills filtered by payment status.
	 * Security: Should be restricted to ADMIN/DOCTOR via SecurityConfig.
	 */
	@GetMapping
	public ResponseEntity<List<BillResponseDTO>> getBillsByStatus(@RequestParam PaymentStatus status,
			@AuthenticationPrincipal User currentUser) {

		logger.info("User {} fetching bills with status {}", currentUser.getUserId(), status);

		// Assuming SecurityConfig restricts this endpoint to ADMIN/DOCTOR
		// e.g. .requestMatchers("/api/bills").hasAnyRole("ADMIN", "DOCTOR")

		List<BillResponseDTO> bills = billingService.getBillsByStatus(status);
		logger.info("Found {} bills with status {}", bills.size(), status);
		return ResponseEntity.ok(bills);
	}

	// ----------------- Modification -----------------

	/**
	 * PUT /api/bills/{billId}/pay Records payment for a bill. Security: Should be
	 * restricted to ADMIN/DOCTOR via SecurityConfig.
	 */
	@PutMapping("/{billId}/pay")
	public ResponseEntity<BillResponseDTO> recordPayment(@PathVariable Long billId,
			@AuthenticationPrincipal User currentUser) {

		logger.info("User {} recording payment for bill {}", currentUser.getUserId(), billId);

		// Assuming SecurityConfig restricts this to ADMIN/DOCTOR
		// If a Patient could pay, the service would need an authorization check here
		// too.

		BillResponseDTO dto = billingService.recordPayment(billId);

		logger.info("Payment recorded for bill {}", billId);
		return ResponseEntity.ok(dto);
	}
}