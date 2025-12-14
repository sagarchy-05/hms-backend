package com.genc.hms.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.genc.hms.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

	// Retrieves patients with an exact name match
	List<Patient> findByName(String name);

	// Retrieves patients whose names contain the given keyword (case-insensitive)
	// Useful for search or autocomplete functionality in UI
	List<Patient> findByNameContainingIgnoreCase(String keyword);
}
