package com.genc.hms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.genc.hms.dto.AppointmentResponseDTO;
import com.genc.hms.dto.BillResponseDTO;
import com.genc.hms.dto.DoctorProfileUpdateDTO;
import com.genc.hms.dto.DoctorRegisterRequestDTO;
import com.genc.hms.dto.DoctorResponseDTO;
import com.genc.hms.dto.PatientRegisterRequestDTO;
import com.genc.hms.dto.PatientResponseDTO;
import com.genc.hms.dto.UserResponseDTO;
import com.genc.hms.dto.UserUpdateRoleAndEmailDTO;
import com.genc.hms.entity.User;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.repository.UserRepository;
import com.genc.hms.util.CustomPasswordEncoder;

@Service
public class AdminService {

    // --- Dependencies ---
    // Injecting dependencies via fields, though constructor injection is preferred.
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; 

    @Autowired
    private PatientService patientService; 

    @Autowired
    private DoctorService doctorService; 

    @Autowired
    private AppointmentService appointmentService; 

    @Autowired
    private BillingService billingService; 

    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;

    // =================================================================================
    // I. USER REGISTRATION (No change needed)
    // =================================================================================

    /**
     * Registers a new Doctor user.
     */
    @Transactional
    public UserResponseDTO registerNewDoctor(DoctorRegisterRequestDTO registrationDetails) {
        return userService.registerDoctor(registrationDetails);
    }

    /**
     * Registers a new Patient user.
     */
    @Transactional
    public UserResponseDTO registerNewPatient(PatientRegisterRequestDTO registrationDetails) {
        return userService.registerPatient(registrationDetails);
    }

    // =================================================================================
    // II. GENERAL USER MANAGEMENT (MODIFIED for error propagation)
    // =================================================================================

    /**
     * Retrieves all users (Admins, Doctors, Patients) with role info.
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getUserId(),
                        userService.getRoleIdFromUser(user),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .toList();
    }

    /**
     * Updates a user's email and/or role.
     * Throws ResourceNotFoundException if user not found.
     */
    @Transactional
    public UserResponseDTO updateUserIdentity(Long userId, UserUpdateRoleAndEmailDTO updateDTO) { // 🚨 Direct DTO return
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        boolean changed = false;

        // Update email if provided
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            user.setEmail(updateDTO.getEmail());
            changed = true;
        }

        // Update role if provided
        if (updateDTO.getRole() != null && updateDTO.getRole() != user.getRole()) {
            user.setRole(updateDTO.getRole());
            changed = true;
        }

        User resultUser = changed ? userRepository.save(user) : user;
        
        Long roleId = userService.getRoleIdFromUser(resultUser);
        return new UserResponseDTO(resultUser.getUserId(), roleId, resultUser.getEmail(),
                resultUser.getRole().name());
    }

    /**
     * Resets a user's password.
     * Throws ResourceNotFoundException if user not found.
     */
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) { // 🚨 Void return
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setPassword(customPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Deletes a user by ID.
     * Throws ResourceNotFoundException if user not found.
     */
    @Transactional
    public void deleteUser(Long userId) { // 🚨 Void return
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        // Note: Cascade/orphan removal should handle associated Doctor/Patient entries.
        userRepository.deleteById(userId);
    }

    // =================================================================================
    // III. ROLE-SPECIFIC MANAGEMENT & RETRIEVAL (MODIFIED where necessary)
    // =================================================================================

    // --- Patient Management ---

    /**
     * Retrieves all patient profiles.
     */
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientService.getAllPatients();
    }

    // --- Doctor Management ---

    /**
     * Retrieves all doctor profiles.
     */
    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorService.findAllDoctors();
    }

    /**
     * Updates a doctor's profile (non-identity fields, e.g., contact, specialization).
     * Delegates to the DoctorService method, which should throw ResourceNotFoundException.
     */
    @Transactional
    public DoctorResponseDTO updateDoctorProfile(Long doctorId, DoctorProfileUpdateDTO updateDTO) { // 🚨 Direct DTO return
        // Assuming doctorService.updateDoctorProfile now returns DTO or throws Exception
        return doctorService.updateDoctorProfile(doctorId, updateDTO).orElseThrow(() -> new ResourceNotFoundException("No Doctor Present with Id : " + doctorId)); 
    }

    // --- Appointment Management ---

    /**
     * Retrieves all appointments.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentService.findAllAppointments();
    }

    // --- Billing Management ---

    /**
     * Retrieves all bills.
     */
    @Transactional(readOnly = true)
    public List<BillResponseDTO> getAllBills() {
        return billingService.getAllBills();
    }

    /**
     * Returns the total number of admin users.
     */
    public Long getCount() {
        return userRepository.getAdminCount();
    }
}