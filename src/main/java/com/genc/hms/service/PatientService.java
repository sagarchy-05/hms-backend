package com.genc.hms.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.genc.hms.dto.PatientResponseDTO;
import com.genc.hms.dto.PatientUpdateRequestDTO;
import com.genc.hms.entity.Patient;
import com.genc.hms.entity.User;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.repository.PatientRepository;

@Service
public class PatientService {

	@Autowired
	private PatientRepository patientRepository;

	// =================================================================================
	// I. PRIVATE UTILITY
	// =================================================================================

	/** Maps Patient entity to PatientResponseDTO including user info. */
	private PatientResponseDTO mapToResponseDTO(Patient patient) {
		User user = patient.getUser();
		// Null check for user is important if the relationship isn't guaranteed
		if (user == null) {
			throw new IllegalStateException("Patient record is not linked to a User account.");
		}
		return new PatientResponseDTO(patient.getPatientId(), user.getUserId(), user.getEmail(), patient.getName(),
				patient.getDob(), patient.getContactNumber(), patient.getAddress(), patient.getGender(),
				patient.getMedicalHistory());
	}

	// =================================================================================
	// II. CREATE
	// =================================================================================

	/** Persists a new Patient entity. */
	@Transactional
	public Patient createPatient(Patient patient) {
		return patientRepository.save(patient);
	}

	// =================================================================================
	// III. UPDATE (MODIFIED for error propagation and DTO return)
	// =================================================================================

	/**
	 * * Updates profile fields of an existing patient. Throws
	 * ResourceNotFoundException if patientId does not exist.
	 */
	@Transactional
	public PatientResponseDTO updatePatientProfile(Long patientId, PatientUpdateRequestDTO updateDTO) {

		// Use orElseThrow to handle the not-found case
		Patient patient = patientRepository.findById(patientId)
				.orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

		patient.setName(updateDTO.getName());
		patient.setDob(updateDTO.getDob());
		patient.setContactNumber(updateDTO.getContactNumber());
		patient.setAddress(updateDTO.getAddress());
		patient.setGender(updateDTO.getGender());
		patient.setMedicalHistory(updateDTO.getMedicalHistory());

		Patient updatedPatient = patientRepository.save(patient);

		// Return the DTO directly after saving
		return mapToResponseDTO(updatedPatient);
	}

	// =================================================================================
	// IV. RETRIEVAL
	// =================================================================================

	/** Retrieves all patients as DTOs. */
	@Transactional(readOnly = true)
	public List<PatientResponseDTO> getAllPatients() {
		return patientRepository.findAll().stream().map(this::mapToResponseDTO).toList();
	}

	/** Retrieves a patient by ID as an Optional DTO. */
	@Transactional(readOnly = true)
	public Optional<PatientResponseDTO> findPatientProfileByPatientId(Long patientId) {
		return patientRepository.findById(patientId).map(this::mapToResponseDTO);
	}

	/** Retrieves a patient entity by ID (for internal use). */
	public Optional<Patient> findById(Long patientId) {
		return patientRepository.findById(patientId);
	}

	// =================================================================================
	// V. SEARCH
	// =================================================================================

	/** Searches patients by keyword in name (case-insensitive). */
	@Transactional(readOnly = true)
	public List<PatientResponseDTO> searchPatient(String keyword) {
		// Return an empty list if the keyword is blank
		if (!StringUtils.hasText(keyword))
			return getAllPatients();

		return patientRepository.findByNameContainingIgnoreCase(keyword).stream().map(this::mapToResponseDTO).toList();
	}

	/** Finds patients by exact name (rarely used). */
	public List<Patient> findByName(String name) {
		return patientRepository.findByName(name);
	}

	// =================================================================================
	// VI. MISC (MODIFIED deletePatient)
	// =================================================================================

	/** Returns total patient count. */
	public Long getCount() {
		return patientRepository.count();
	}

	/**
	 * * Deletes a patient by ID. Throws ResourceNotFoundException if patientId does
	 * not exist.
	 */
	@Transactional
	public void deletePatient(long id) {
		// Use existsById for a potentially lighter query than findById
		if (!patientRepository.existsById(id)) {
			throw new ResourceNotFoundException("Patient not found with ID: " + id);
		}
		patientRepository.deleteById(id);
	}
}