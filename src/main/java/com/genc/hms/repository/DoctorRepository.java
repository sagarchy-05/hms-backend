package com.genc.hms.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.genc.hms.entity.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Searches doctors by name or specialization (case-insensitive, partial match)
    // Useful for implementing search functionality in frontend or admin dashboard
    List<Doctor> findByNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(
            String nameKeyword, String specializationKeyword);
}
