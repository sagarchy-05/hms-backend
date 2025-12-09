package com.genc.hms.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DoctorRegisterRequestDTO {

	// --- User Identity Fields ---
	@NotBlank(message = "Email is required.")
	@Email(message = "Email format is invalid.")
	@Size(max = 100, message = "Email cannot exceed 100 characters.")
	private String email;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 255, message = "Password must be at least 5 characters.")
	private String password;

	// --- Doctor Profile Fields ---
	@NotBlank(message = "Doctor name is required.")
	@Size(max = 100, message = "Name cannot exceed 100 characters.")
	private String name;

	@NotBlank(message = "Specialization is required.")
	@Size(max = 100, message = "Specialization cannot exceed 100 characters.")
	private String specialization;

	@NotBlank(message = "Contact number is required.")
	@Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.")
	private String contactNumber;

	@NotNull(message = "Consultation fee is required.")
	@DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee cannot be negative.")
	private BigDecimal consultationFee;

	@Valid
	@NotNull(message = "Doctor availability schedule is required.")
	private List<DoctorAvailabilityDTO> doctorAvailabilities;

	public DoctorRegisterRequestDTO() {
	}

	public DoctorRegisterRequestDTO(
			@NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") @Size(max = 100, message = "Email cannot exceed 100 characters.") String email,
			@NotBlank(message = "Password is required.") @Size(min = 5, max = 255, message = "Password must be at least 5 characters.") String password,
			@NotBlank(message = "Doctor name is required.") @Size(max = 100, message = "Name cannot exceed 100 characters.") String name,
			@NotBlank(message = "Specialization is required.") @Size(max = 100, message = "Specialization cannot exceed 100 characters.") String specialization,
			@NotBlank(message = "Contact number is required.") @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.") String contactNumber,
			@NotNull(message = "Consultation fee is required.") @DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee cannot be negative.") BigDecimal consultationFee,
			@Valid @NotNull(message = "Doctor availability schedule is required.") List<DoctorAvailabilityDTO> doctorAvailabilities) {
		super();
		this.email = email;
		this.password = password;
		this.name = name;
		this.specialization = specialization;
		this.contactNumber = contactNumber;
		this.consultationFee = consultationFee;
		this.doctorAvailabilities = doctorAvailabilities;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public BigDecimal getConsultationFee() {
		return consultationFee;
	}

	public void setConsultationFee(BigDecimal consultationFee) {
		this.consultationFee = consultationFee;
	}

	public List<DoctorAvailabilityDTO> getDoctorAvailabilities() {
		return doctorAvailabilities;
	}

	public void setDoctorAvailabilities(List<DoctorAvailabilityDTO> doctorAvailabilities) {
		this.doctorAvailabilities = doctorAvailabilities;
	}
}