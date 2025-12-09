package com.genc.hms.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize; // 🚨 New Import for Role Check
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 🚨 New Import for User Context
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.genc.hms.dto.AdminRegisterDTO;
import com.genc.hms.dto.AdminResponseDTO;
import com.genc.hms.dto.AppointmentResponseDTO;
import com.genc.hms.dto.BillResponseDTO;
import com.genc.hms.dto.DoctorProfileUpdateDTO;
import com.genc.hms.dto.DoctorRegisterRequestDTO;
import com.genc.hms.dto.DoctorResponseDTO;
import com.genc.hms.dto.PatientRegisterRequestDTO;
import com.genc.hms.dto.PatientResponseDTO;
import com.genc.hms.dto.PatientUpdateRequestDTO;
import com.genc.hms.dto.UserResponseDTO;
import com.genc.hms.dto.UserUpdateRoleAndEmailDTO;
import com.genc.hms.entity.User;
import com.genc.hms.enums.AppointmentStatus;
import com.genc.hms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.genc.hms.service.AdminService;
import com.genc.hms.service.AppointmentService;
import com.genc.hms.service.BillingService;
import com.genc.hms.service.DoctorService;
import com.genc.hms.service.PatientService;
import com.genc.hms.service.UserService;

import jakarta.validation.Valid;

/**
 * REST Controller for administrator-level operations in the Hospital Management System.
 * All endpoints are secured by @PreAuthorize("hasRole('ADMIN')") and use JWT context.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // 🚨 SECURES ALL METHODS IN THIS CONTROLLER
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AdminService adminService;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    // 🚨 Use full constructor injection for all dependencies
    public AdminController(
            DoctorService doctorService, 
            PatientService patientService,
            AdminService adminService,
            UserService userService,
            AppointmentService appointmentService,
            BillingService billingService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.adminService = adminService;
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
    }

    // ===================== I. USER MANAGEMENT =====================
    
    /** 🚨 All security checks (isAdmin) are now handled by @PreAuthorize on the class level. **/
    
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@AuthenticationPrincipal User adminUser) {
        logger.info("Admin [{}] requested all users", adminUser.getUserId());
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id, @AuthenticationPrincipal User adminUser) {
        logger.info("Admin [{}] requested user with id {}", adminUser.getUserId(), id);
        
        // Assuming userService.getUserById throws ResourceNotFoundException on null
        UserResponseDTO user = userService.getUserById(id); 
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}/identity")
    public ResponseEntity<UserResponseDTO> updateUserIdentity(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRoleAndEmailDTO updateDTO,
            @AuthenticationPrincipal User adminUser) {

        // Check for email conflict (needs to remain in controller or be delegated to a validation layer)
        if (updateDTO.getEmail() != null && userService.findByEmail(updateDTO.getEmail())
                .filter(u -> !u.getUserId().equals(userId)).isPresent()) {
            logger.warn("Admin [{}] attempted to update user [{}] with duplicate email {}",
                    adminUser.getUserId(), userId, updateDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Service should return DTO or throw ResourceNotFoundException
        UserResponseDTO updatedUser = adminService.updateUserIdentity(userId, updateDTO);
        logger.info("Admin [{}] updated user [{}]", adminUser.getUserId(), userId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<String> resetPassword(@PathVariable Long userId, @RequestBody String password, @AuthenticationPrincipal User adminUser) {
        
        // Service should throw ResourceNotFoundException if user ID is invalid
        adminService.resetUserPassword(userId, password);
        logger.info("Admin [{}] reset password for user [{}]", adminUser.getUserId(), userId);
        return ResponseEntity.ok("Password successfully reset for user ID: " + userId);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, @AuthenticationPrincipal User adminUser) {
        
        // Service should throw ResourceNotFoundException if deletion target is invalid
        adminService.deleteUser(userId);
        logger.info("Admin [{}] deleted user [{}]", adminUser.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    // ===================== II. REGISTRATION =====================
    
    // Note: The /admin/register path needs to be updated to /register to follow REST convention /api/admin/register

    @PostMapping("/register")
    public ResponseEntity<AdminResponseDTO> registerAdmin(@Valid @RequestBody AdminRegisterDTO registrationDetails, @AuthenticationPrincipal User adminUser) {
        
        // Only allow first admin to register without being authenticated (handled in service or security config)
        // For existing admins, the @PreAuthorize handles the role check.
        Long count = adminService.getCount();
        if (count != 0 && adminUser == null) { // This block is hard to implement cleanly here. Better handled in the Service/Security layer
             throw new AccessDeniedException("Only the first admin can be registered without authorization.");
        }
        
        if (userService.findByEmail(registrationDetails.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        AdminResponseDTO dto = userService.registerAdmin(registrationDetails);
        logger.info("Admin [{}] registered new admin [{}]", adminUser != null ? adminUser.getUserId() : "SYSTEM", dto.getUserId());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/doctors/register")
    public ResponseEntity<UserResponseDTO> registerDoctor(@Valid @RequestBody DoctorRegisterRequestDTO registrationDetails, @AuthenticationPrincipal User adminUser) {
        if (userService.findByEmail(registrationDetails.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserResponseDTO dto = adminService.registerNewDoctor(registrationDetails);
        logger.info("Admin [{}] registered new doctor [{}]", adminUser.getUserId(), dto.getUserId());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/patients/register")
    public ResponseEntity<UserResponseDTO> registerPatient(@Valid @RequestBody PatientRegisterRequestDTO registrationDetails, @AuthenticationPrincipal User adminUser) {
        if (userService.findByEmail(registrationDetails.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserResponseDTO dto = adminService.registerNewPatient(registrationDetails);
        logger.info("Admin [{}] registered new patient [{}]", adminUser.getUserId(), dto.getUserId());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // ===================== III. DOCTOR MANAGEMENT =====================
    
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors(@AuthenticationPrincipal User adminUser) {
        logger.info("Admin [{}] requested all doctors", adminUser.getUserId());
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long doctorId, @AuthenticationPrincipal User adminUser) {
        
        // Service throws ResourceNotFoundException if doctorId is invalid
        DoctorResponseDTO doctor = doctorService.findDoctorById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> updateDoctorProfile(
            @PathVariable Long doctorId, 
            @Valid @RequestBody DoctorProfileUpdateDTO updateDTO, 
            @AuthenticationPrincipal User adminUser) {

        // Service throws ResourceNotFoundException if doctorId is invalid
        DoctorResponseDTO updated = adminService.updateDoctorProfile(doctorId, updateDTO);
        logger.info("Admin [{}] updated doctor [{}]", adminUser.getUserId(), doctorId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable long id, @AuthenticationPrincipal User adminUser) {
        
        // Service throws ResourceNotFoundException if deletion target is invalid
        doctorService.deleteDoctor(id);
        logger.info("Admin [{}] deleted doctor [{}]", adminUser.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    // ===================== IV. PATIENT MANAGEMENT =====================
    
    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients(@AuthenticationPrincipal User adminUser) {
        logger.info("Admin [{}] requested all patients", adminUser.getUserId());
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable long id, @AuthenticationPrincipal User adminUser) {
        
        // Service throws ResourceNotFoundException if patientId is invalid
        PatientResponseDTO patient = patientService.findPatientProfileByPatientId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/patients/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatientProfile(
            @PathVariable long id, 
            @RequestBody PatientUpdateRequestDTO updateRequestDTO, 
            @AuthenticationPrincipal User adminUser) {

        // Service throws ResourceNotFoundException if patientId is invalid
        PatientResponseDTO dto = patientService.updatePatientProfile(id, updateRequestDTO);
        logger.info("Admin [{}] updated patient [{}]", adminUser.getUserId(), id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable long id, @AuthenticationPrincipal User adminUser) {
        
        // Service throws ResourceNotFoundException if deletion target is invalid
        patientService.deletePatient(id);
        logger.info("Admin [{}] deleted patient [{}]", adminUser.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    // ===================== V. APPOINTMENT MANAGEMENT =====================
    
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments(@AuthenticationPrincipal User adminUser) {
        logger.info("Admin [{}] requested all appointments", adminUser.getUserId());
        return ResponseEntity.ok(adminService.getAllAppointments());
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable long id, @AuthenticationPrincipal User adminUser) {
        
        // Service throws ResourceNotFoundException if appointment is missing or conversion fails
        AppointmentResponseDTO dto = appointmentService.findAppointmentById(id).orElseThrow(() -> new ResourceNotFoundException("No appointment found : " + id));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentResponseDTO> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus newStatus,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal User adminUser) {

        // Service throws ResourceNotFoundException if appointment is missing
        AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointmentStatus(appointmentId, newStatus, remarks);
        return ResponseEntity.ok(updatedAppointment);
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long appointmentId, @AuthenticationPrincipal User adminUser) {
        
        // Service should throw a specific exception (e.g., IllegalStateException or ResourceNotFoundException)
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    // ===================== VI. BILLING MANAGEMENT =====================
    
    @GetMapping("/billing")
    public ResponseEntity<List<BillResponseDTO>> getAllBills(@AuthenticationPrincipal User adminUser) {
        logger.info("Admin [{}] requested all bills", adminUser.getUserId());
        return ResponseEntity.ok(adminService.getAllBills());
    }

    @PostMapping("/billing/{billId}/pay")
    public ResponseEntity<BillResponseDTO> recordPayment(@PathVariable Long billId, @AuthenticationPrincipal User adminUser) {
        
        // Service throws ResourceNotFoundException if bill is missing
        BillResponseDTO updatedBill = billingService.recordPayment(billId);
        return ResponseEntity.ok(updatedBill);
    }

    // ===================== VII. COUNT MANAGEMENT =====================
    
    @GetMapping("/count")
    public ResponseEntity<Long> getCount(@RequestParam String name, @AuthenticationPrincipal User adminUser) {
        
        Long count = switch (name.toLowerCase()) {
            case "doctors" -> doctorService.getCount();
            case "patients" -> patientService.getCount();
            case "admins" -> adminService.getCount();
            case "users" -> userService.getCount();
            case "appointments" -> appointmentService.getCount();
            case "bills" -> billingService.getCount();
            default -> null;
        };

        if (count == null) {
            logger.warn("Admin [{}] requested invalid count type: {}", adminUser.getUserId(), name);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(count);
    }
}