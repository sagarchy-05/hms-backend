package com.genc.hms.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.genc.hms.dto.*;
import com.genc.hms.entity.*;
import com.genc.hms.enums.Role;
import com.genc.hms.repository.UserRepository;
import com.genc.hms.util.CustomPasswordEncoder;

import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;

    // =================================================================================
    // I. UTILITY & LOOKUP METHODS
    // =================================================================================

    /**
     * Returns role-specific ID (PatientId, DoctorId) or generic userId for Admins.
     */
    public Long getRoleIdFromUser(User user) {
        if (user.getRole() == Role.PATIENT && user.getPatientProfile() != null) {
            return user.getPatientProfile().getPatientId();
        } else if (user.getRole() == Role.DOCTOR && user.getDoctorProfile() != null) {
            return user.getDoctorProfile().getDoctorId();
        }
        return user.getUserId();
    }

    /** Find user by generic User ID. */
    public Optional<User> findById(long id) {
        return userRepo.findById(id);
    }

    /** Find user by email (username). */
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    // =================================================================================
    // II. REGISTRATION / CREATION & Login
    // =================================================================================

    /**
     * Registers a new Patient and links User -> Patient entities.
     * Cascade ensures both are persisted in one transaction.
     */
    @Transactional
    public UserResponseDTO registerPatient(@Valid @RequestBody PatientRegisterRequestDTO request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setRole(Role.PATIENT);
        user.setPassword(customPasswordEncoder.encode(request.getPassword()));

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setDob(request.getDob());
        patient.setContactNumber(request.getContactNumber());
        patient.setAddress(request.getAddress());
        patient.setGender(request.getGender());
        patient.setMedicalHistory(request.getMedicalHistory());

        patient.setUser(user);
        user.setPatientProfile(patient);

        User savedUser = userRepo.save(user);
        Patient savedPatient = savedUser.getPatientProfile();

        return new UserResponseDTO(savedUser.getUserId(), savedPatient.getPatientId(), savedUser.getEmail(),
                savedUser.getRole().name());
    }

    /**
     * Registers a new Doctor with profile and availability slots.
     * Links User -> Doctor -> DoctorAvailability entities for persistence.
     */
    @Transactional
    public UserResponseDTO registerDoctor(@Valid @RequestBody DoctorRegisterRequestDTO request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setRole(Role.DOCTOR);
        user.setPassword(customPasswordEncoder.encode(request.getPassword()));

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setContactNumber(request.getContactNumber());
        doctor.setConsultationFee(request.getConsultationFee());

        List<DoctorAvailability> availabilities = request.getDoctorAvailabilities().stream().map(availDto -> {
            DoctorAvailability availability = new DoctorAvailability();
            availability.setDoctor(doctor);
            availability.setDayOfWeek(availDto.getDayOfWeek());
            availability.setStartTime(availDto.getStartTime());
            availability.setEndTime(availDto.getEndTime());
            return availability;
        }).toList();

        doctor.setDoctorAvailabilities(availabilities);
        doctor.setUser(user);
        user.setDoctorProfile(doctor);

        User savedUser = userRepo.save(user);
        Doctor savedDoctor = savedUser.getDoctorProfile();

        return new UserResponseDTO(savedUser.getUserId(), savedDoctor.getDoctorId(), savedUser.getEmail(),
                savedUser.getRole().name());
    }

    /**
     * Registers an Admin user; Admins do not have linked profiles.
     */
    @Transactional
    public AdminResponseDTO registerAdmin(@Valid @RequestBody AdminRegisterDTO request) {
        String encodedPassword = customPasswordEncoder.encode(request.getPassword());
        User user = new User(null, null, request.getEmail(), encodedPassword, Role.ADMIN);
        User savedUser = userRepo.save(user);

        Optional<User> savedUserOptional = userRepo.findById(savedUser.getUserId());
        if(savedUserOptional.isEmpty())
        	return new AdminResponseDTO();
        savedUser = savedUserOptional.get();
        return new AdminResponseDTO(savedUser.getUserId(), savedUser.getEmail());
    }

    /**
     * Login for User.
     */
    @Transactional
    public UserLoginResponseDTO login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        boolean isValidPassword = customPasswordEncoder.matches(password, user.getPassword());
        if (!isValidPassword) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        long roleId = getRoleIdFromUser(user);
        return new UserLoginResponseDTO(
                user.getUserId(),
                roleId,
                user.getEmail(),
                user.getRole().toString(),
                "Login successful!"
        );
    }


    // =================================================================================
    // III. PASSWORD MANAGEMENT
    // =================================================================================

    /** Allows user to change own password after verifying current password. */
    @Transactional
    public boolean changePassword(String authenticatedEmail, UserChangePasswordRequestDTO request) {
        Optional<User> userOptional = userRepo.findByEmail(authenticatedEmail);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();
        if (!customPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())) return false;
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) return false;

        user.setPassword(customPasswordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        return true;
    }

    /** Allows Admin to reset any user's password directly. */
    @Transactional
    public boolean adminResetPassword(Long userId, String newPassword) {
        Optional<User> userOptional = userRepo.findById(userId);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();
        user.setPassword(customPasswordEncoder.encode(newPassword));
        userRepo.save(user);
        return true;
    }

    // =================================================================================
    // IV. RETRIEVAL & ADMIN MANAGEMENT
    // =================================================================================

    /** Returns all users with role-specific IDs for management or UI display. */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(user -> new UserResponseDTO(user.getUserId(), getRoleIdFromUser(user),
                        user.getEmail(), user.getRole().name()))
                .toList();
    }

    /**
     * Updates user's email and/or role.
     * Returns Optional.empty() if user not found or new email conflicts.
     */
    @Transactional
    public Optional<UserResponseDTO> adminUpdateUserRoleAndEmail(Long userId, UserUpdateRoleAndEmailDTO updateDTO) {
        Optional<User> userOptional = userRepo.findById(userId);
        if (userOptional.isEmpty()) return Optional.empty();

        User user = userOptional.get();

        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepo.findByEmail(updateDTO.getEmail()).isPresent()) return Optional.empty();
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getRole() != null && updateDTO.getRole() != user.getRole()) {
            user.setRole(updateDTO.getRole());
        }

        User savedUser = userRepo.save(user);
        return Optional.of(new UserResponseDTO(savedUser.getUserId(), getRoleIdFromUser(savedUser),
                savedUser.getEmail(), savedUser.getRole().name()));
    }

    /** Returns the total number of users in the system. */
    public Long getCount() {
        return userRepo.count();
    }

    /** Retrieves a single user by ID with role-specific ID in DTO. */
    public UserResponseDTO getUserById(long id) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isEmpty()) return null;

        User user = userOptional.get();
        return new UserResponseDTO(user.getUserId(), getRoleIdFromUser(user), user.getEmail(), user.getRole().toString());
    }
}
