package com.genc.hms.dto;

import java.time.LocalDate;

import com.genc.hms.enums.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PatientUpdateRequestDTO {

	@NotBlank(message = "Name is required.")
	@Size(max = 100, message = "Name cannot exceed 100 characters.")
	private String name;

	@NotNull(message = "Date of Birth is required.")
	private LocalDate dob;

	@NotBlank(message = "Contact number is required.")
	@Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.")
	private String contactNumber;

	@NotBlank(message = "Address is required.")
	private String address;

	@NotNull(message = "Gender is required.")
	private Gender gender;

	// Medical history can be nullable or empty
	private String medicalHistory;

	// Constructors
	public PatientUpdateRequestDTO() {
		// default constructor
	}

	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getMedicalHistory() {
		return medicalHistory;
	}

	public void setMedicalHistory(String medicalHistory) {
		this.medicalHistory = medicalHistory;
	}
}
