package com.genc.hms.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.genc.hms.dto.DoctorAvailabilityDTO;
import com.genc.hms.dto.DoctorProfileUpdateDTO;
import com.genc.hms.dto.DoctorResponseDTO;
import com.genc.hms.entity.Doctor;
import com.genc.hms.entity.DoctorAvailability;
import com.genc.hms.enums.WeekDay;
import com.genc.hms.repository.DoctorRepository;

@Service
public class DoctorService {

	@Autowired
	private DoctorRepository doctorRepository;

    // =================================================================================
    // I. PRIVATE UTILITY (Mappers)
    // =================================================================================

    /**
     * Converts DoctorAvailability entity to DTO.
     */
    private DoctorAvailabilityDTO mapAvailabilityToDTO(DoctorAvailability availability) {
        return new DoctorAvailabilityDTO(
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }

    /**
     * Converts Doctor entity to DTO including all availabilities.
     */
    private DoctorResponseDTO mapDoctorToDTO(Doctor doctor) {
        List<DoctorAvailabilityDTO> availabilities = doctor.getDoctorAvailabilities().stream()
                .map(this::mapAvailabilityToDTO)
                .toList();

        return new DoctorResponseDTO(
                doctor.getDoctorId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getContactNumber(),
                doctor.getConsultationFee(),
                availabilities
        );
    }

    // =================================================================================
    // II. CREATE & UPDATE
    // =================================================================================

    /**
     * Saves a new Doctor entity.
     */
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    /**
     * Updates a doctor's profile and replaces the availability schedule.
     * Clears the managed collection to ensure JPA orphan removal works correctly.
     */
    @Transactional
    public Optional<DoctorResponseDTO> updateDoctorProfile(Long doctorId, DoctorProfileUpdateDTO updateDTO) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);
        if (doctorOptional.isEmpty()) return Optional.empty();

        Doctor doctor = doctorOptional.get();
        doctor.setName(updateDTO.getName());
        doctor.setSpecialization(updateDTO.getSpecialization());
        doctor.setContactNumber(updateDTO.getContactNumber());
        doctor.setConsultationFee(updateDTO.getConsultationFee());

        // Clear existing availabilities to delete old entries
        doctor.getDoctorAvailabilities().clear();

        // Add new availabilities, ensuring entities are linked to the doctor
        List<DoctorAvailability> newAvailabilities = updateDTO.getDoctorAvailabilities().stream()
                .map(dto -> {
                    DoctorAvailability availability = new DoctorAvailability();
                    availability.setDoctor(doctor);
                    availability.setDayOfWeek(dto.getDayOfWeek());
                    availability.setStartTime(dto.getStartTime());
                    availability.setEndTime(dto.getEndTime());
                    return availability;
                }).toList();

        doctor.getDoctorAvailabilities().addAll(newAvailabilities);

        return Optional.of(mapDoctorToDTO(doctorRepository.save(doctor)));
    }

    // =================================================================================
    // III. RETRIEVAL
    // =================================================================================

    /**
     * Returns all doctors mapped to DTOs.
     */
    public List<DoctorResponseDTO> findAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapDoctorToDTO)
                .toList();
    }

    /**
     * Retrieves a doctor by ID, returning the DTO.
     */
    @Transactional(readOnly = true)
    public Optional<DoctorResponseDTO> findDoctorById(Long id) {
        return doctorRepository.findById(id).map(this::mapDoctorToDTO);
    }

    /**
     * Retrieves raw Doctor entity by ID (used internally in services like Billing).
     */
    public Optional<Doctor> findById(Long doctorId) {
        return doctorRepository.findById(doctorId);
    }

    /**
     * Searches doctors by name or specialization.
     * Returns all doctors if keyword is blank.
     */
    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> searchDoctors(String keyword) {
        if (!StringUtils.hasText(keyword)) return findAllDoctors();

        return doctorRepository
                .findByNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(keyword, keyword)
                .stream().map(this::mapDoctorToDTO)
                .toList();
    }

    /**
     * Returns unique weekdays when the doctor is available.
     */
    @Transactional(readOnly = true)
    public List<WeekDay> getDoctorAvailableDays(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return new ArrayList<>();

        return doctor.getDoctorAvailabilities().stream()
                .map(DoctorAvailability::getDayOfWeek)
                .distinct()
                .toList();
    }

    /**
     * Returns detailed availability schedule of a doctor.
     */
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityDTO> getDoctorAvailability(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(d -> d.getDoctorAvailabilities().stream()
                        .map(this::mapAvailabilityToDTO)
                        .toList())
                .orElse(null);
    }

    /**
     * Returns the consultation fee of a doctor.
     */
    public BigDecimal getDoctorFee(Doctor doctor) {
        return doctor.getConsultationFee();
    }

    public Long getCount() {
        return doctorRepository.count();
    }

    /**
     * Deletes a doctor by ID. Returns false if doctor not found.
     */
    @Transactional
    public boolean deleteDoctor(long id) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isEmpty()) return false;

        doctorRepository.delete(doctorOpt.get());
        return true;
    }
}
