package com.genc.hms.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.genc.hms.dto.DoctorResponseDTO;
import com.genc.hms.dto.PatientResponseDTO;
import com.genc.hms.dto.PatientUpdateRequestDTO;
import com.genc.hms.entity.Patient;
import com.genc.hms.entity.User;
import com.genc.hms.enums.Role;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.service.DoctorService;
import com.genc.hms.service.PatientService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping("/api/patients")
public class PatientController {

	private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

	private final PatientService patientService;
	private final DoctorService doctorService;

    // Use constructor injection
	public PatientController(PatientService patientService, DoctorService doctorService) {
		this.patientService = patientService;
		this.doctorService = doctorService;
	}

    // Remove: getAuthorizedPatientId helper method

	/**
	 * Helper method to enforce that the authenticated user is a PATIENT and retrieve
	 * their Patient ID.
	 */
	private Long getAuthenticatedPatientId(@AuthenticationPrincipal User currentUser) {
		// Ensure the user has the PATIENT role AND the patient profile link exists
		if (currentUser.getRole() != Role.PATIENT || currentUser.getPatientProfile() == null) {
			logger.warn("User {} attempted to access patient resource without PATIENT role.", currentUser.getUserId());
			throw new AccessDeniedException("User is not authorized to access patient resources.");
		}
		// Assuming User entity links to PatientProfile, which has getPatientId()
		return currentUser.getPatientProfile().getPatientId();
	}

	// ----------------- Self-Management -----------------

	/**
	 * GET /api/patients/me - Retrieves the profile of the authenticated PATIENT.
	 */
	@GetMapping("/me")
	public ResponseEntity<PatientResponseDTO> getPatientProfile(@AuthenticationPrincipal User currentUser) {
		
		// 1. Authorization check: Ensures the user is a PATIENT and retrieves their ID
		Long patientId = getAuthenticatedPatientId(currentUser);

		// 2. Delegate retrieval to the main method
		return getPatientFromId(patientId);
	}

	/**
	 * PUT /api/patients/me - Updates the profile of the authenticated PATIENT.
	 */
	@PutMapping("/me")
	public ResponseEntity<PatientResponseDTO> updatePatientProfile(
			@Valid @RequestBody PatientUpdateRequestDTO updateDTO,
			@AuthenticationPrincipal User currentUser) { // Inject User instead of Session

		// 1. Authorization check: Ensures the user is a PATIENT and retrieves their ID
		Long patientId = getAuthenticatedPatientId(currentUser);

		// 2. Perform the update using the authenticated ID
		PatientResponseDTO patient = patientService.updatePatientProfile(patientId, updateDTO);

		logger.info("Patient {} updated their profile", patientId);
		return ResponseEntity.ok(patient);
	}

	// ----------------- General Retrieval (Staff/Admin) -----------------

	@GetMapping
	public ResponseEntity<List<PatientResponseDTO>> getAllOrSearchPatients(
			@RequestParam(required = false) String keyword) {

		List<PatientResponseDTO> patientResponseDto = patientService.searchPatient(keyword);
		logger.info("Retrieved list of patients (search keyword: {})", keyword);
		return ResponseEntity.ok(patientResponseDto);
	}

	/**
	 * GET /api/patients/{id} - Retrieves a patient by their Patient ID.
     * Note: This method allows ADMINs/Staff to fetch any patient. 
     * If a PATIENT calls this, SecurityConfig or an internal check should limit them to their own ID.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<PatientResponseDTO> getPatientFromId(@PathVariable Long id) {
		// Note: The previous logic relied on manual DTO conversion, which is fine, 
		// but using a service method that returns the DTO is cleaner.

        // If your service doesn't return DTO, keep the DTO conversion logic here:
		Optional<Patient> patientOptional = patientService.findById(id);

		if (patientOptional.isEmpty()) {
			logger.warn("Patient with ID {} not found", id);
			throw new ResourceNotFoundException("Patient not found with ID: " + id);
		}

		Patient patient = patientOptional.get();
		PatientResponseDTO dto = new PatientResponseDTO(patient.getPatientId(), patient.getUser().getUserId(),
				patient.getUser().getEmail(), patient.getName(), patient.getDob(), patient.getContactNumber(),
				patient.getAddress(), patient.getGender(), patient.getMedicalHistory());

		logger.info("Retrieved details of patient {}", id);
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@GetMapping("/find-doctor")
	public ResponseEntity<List<DoctorResponseDTO>> getAllOrSearchDoctors(
			@RequestParam(required = false) String keyword) {
		List<DoctorResponseDTO> doctors = doctorService.searchDoctors(keyword);
		logger.info("Retrieved list of doctors with search keyword: {}", keyword != null ? keyword : "N/A");
		return ResponseEntity.ok(doctors);
	}
}