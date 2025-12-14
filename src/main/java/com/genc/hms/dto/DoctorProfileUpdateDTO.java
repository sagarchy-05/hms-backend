package com.genc.hms.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO used by the Admin or Doctor to update the Doctor's profile information.
 * Excludes email and password, which are handled by the UserService.
 */
public class DoctorProfileUpdateDTO {

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

	// Constructors
	public DoctorProfileUpdateDTO() {
		// default constructor
	}

	// Getters and Setters
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
