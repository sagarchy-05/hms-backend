package com.genc.hms.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.genc.hms.dto.DoctorAvailabilityDTO;
import com.genc.hms.dto.DoctorResponseDTO;
import com.genc.hms.entity.User;
import com.genc.hms.enums.Role;
import com.genc.hms.enums.WeekDay;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.service.DoctorService;

@RestController
@CrossOrigin
@RequestMapping("/api/doctors")
public class DoctorController {

	private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

	// Use constructor injection instead of @Autowired field injection
	private final DoctorService doctorService;

	public DoctorController(DoctorService doctorService) {
		this.doctorService = doctorService;
	}

	/**
	 * Helper method to enforce that the authenticated user is a DOCTOR and retrieve
	 * their Doctor ID.
	 */
	private Long getAuthenticatedDoctorId(@AuthenticationPrincipal User currentUser) {
		if (currentUser.getRole() != Role.DOCTOR || currentUser.getDoctorProfile() == null) {
			// Throw AccessDenied if the user is not a DOCTOR or their profile link is
			// missing
			throw new AccessDeniedException("User is not authorized to access doctor-specific resources.");
		}
		// Assuming your User entity has a link to the Doctor profile to get the
		// role-specific ID
		return currentUser.getDoctorProfile().getDoctorId();
	}

	// ----------------- Self Profile (Refactored for JWT) -----------------

	/**
	 * GET /api/doctors/me - Retrieves the profile of the authenticated DOCTOR. DB
	 * Optimized: Uses the already loaded User principal to get the Doctor ID (zero
	 * DB call for ID).
	 */
	@GetMapping("/me")
	public ResponseEntity<DoctorResponseDTO> getCurrentDoctor(@AuthenticationPrincipal User currentUser) {

		// 1. Authorization check: Ensures the user is a DOCTOR
		Long doctorId = getAuthenticatedDoctorId(currentUser);

		// 2. Delegate retrieval to the optimized public method
		return getDoctorById(doctorId, currentUser);
	}

	/**
	 * GET /api/doctors/{id} - Retrieves a doctor by their Doctor ID. Authorization:
	 * Allows DOCTORs to view their own ID; ADMIN/PATIENT/other DOCTORs are allowed
	 * by SecurityConfig. DB Optimization: Leverages Optional.orElseThrow for clean
	 * error handling.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id,
			@AuthenticationPrincipal User currentUser) { // Injecting User for logging/context

		// Authorization check (optional, as SecurityConfig handles roles, but good for
		// self-management check)
		// If the user is a Doctor, we might enforce they can only see their own
		// profile,
		// but for a general retrieval endpoint, we rely on SecurityConfig:
		// .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

		// Use orElseThrow to handle the not-found case, relying on
		// GlobalExceptionHandler (404)
		DoctorResponseDTO doctor = doctorService.findDoctorById(id).orElseThrow(() -> {
			logger.warn("Doctor profile not found for ID {}", id);
			return new ResourceNotFoundException("Doctor not found with ID: " + id);
		});

		logger.info("User {} retrieved doctor profile for ID {}", currentUser.getUserId(), id);
		return ResponseEntity.ok(doctor);
	}

	// ----------------- General Retrieval & Search -----------------

	/**
	 * GET /api/doctors/all - Retrieves list of all doctors.
	 */
	@GetMapping("/all")
	public List<DoctorResponseDTO> getAllDoctors() {
		logger.info("Retrieved list of all doctors");
		return doctorService.findAllDoctors();
	}

	/**
	 * GET /api/doctors - Retrieves all or searches doctors by keyword.
	 */
	@GetMapping
	public ResponseEntity<List<DoctorResponseDTO>> getAllOrSearchDoctors(
			@RequestParam(required = false) String keyword) {
		List<DoctorResponseDTO> doctors = doctorService.searchDoctors(keyword);
		logger.info("Retrieved list of doctors with search keyword: {}", keyword != null ? keyword : "N/A");
		return ResponseEntity.ok(doctors);
	}

	// ----------------- Availability -----------------

	/**
	 * GET /api/doctors/{doctorId}/availability - Retrieves full availability
	 * schedule.
	 */
	@GetMapping("/{doctorId}/availability")
	public List<DoctorAvailabilityDTO> getDoctorAvailability(@PathVariable Long doctorId) {
		logger.info("Retrieved full availability for doctor {}", doctorId);
		// Assuming service throws ResourceNotFoundException if doctorId is invalid
		return doctorService.getDoctorAvailability(doctorId);
	}

	/**
	 * GET /api/doctors/{doctorId}/available-days - Retrieves list of available
	 * days. Optimized: Uses Optional from service and orElseThrow.
	 */
	@GetMapping("/{doctorId}/available-days")
	public ResponseEntity<List<WeekDay>> getDoctorAvailableDays(@PathVariable Long doctorId) {

		// Assuming service method returns an Optional for clean NOT_FOUND handling
		List<WeekDay> availableDays = doctorService.getDoctorAvailableDays(doctorId);

		logger.info("Retrieved available days for doctor {}", doctorId);
		return ResponseEntity.ok(availableDays);
	}
}