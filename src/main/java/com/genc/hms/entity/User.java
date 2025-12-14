package com.genc.hms.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.genc.hms.enums.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	// If the user is a patient, this maps to their patient profile
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Patient patientProfile;

	// If the user is a doctor, this maps to their doctor profile
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Doctor doctorProfile;

	// Unique email serves as username for login
	@NotBlank(message = "Email (Username) is required.")
	@Email(message = "Email format is invalid.")
	@Size(max = 100, message = "Email cannot exceed 100 characters.")
	@Column(unique = true, nullable = false, length = 100)
	private String email;

	// Encrypted password stored securely
	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
	@Column(nullable = false)
	private String password;

	// Defines the user’s role in the system (e.g., ADMIN, DOCTOR, PATIENT)
	@NotNull(message = "User role is required.")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	public User() {
	}

	public User(Patient patientProfile, Doctor doctorProfile, String email, String password, Role role) {
		this.patientProfile = patientProfile;
		this.doctorProfile = doctorProfile;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Patient getPatientProfile() {
		return patientProfile;
	}

	public void setPatientProfile(Patient patientProfile) {
		this.patientProfile = patientProfile;
	}

	public Doctor getDoctorProfile() {
		return doctorProfile;
	}

	public void setDoctorProfile(Doctor doctorProfile) {
		this.doctorProfile = doctorProfile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", email=" + email + ", role=" + role + "]";
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return email;
	}
}
