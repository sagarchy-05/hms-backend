package com.genc.hms.dto;

import java.time.LocalDate;

import com.genc.hms.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PatientRegisterRequestDTO {

	@NotBlank(message = "Email is required.")
	@Email(message = "Email format is invalid.")
	@Size(max = 100, message = "Email cannot exceed 100 characters.")
	private String email;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 255, message = "Password must be at least 8 characters.")
	private String password;

	@NotBlank(message = "Name is required.")
	@Size(max = 100, message = "Name cannot exceed 100 characters.")
	private String name;

	@NotNull(message = "Date of Birth is required.")
	@Past(message = "Date of Birth must be in the past.")
	private LocalDate dob;

	@NotNull(message = "Gender is required.")
	private Gender gender;

	@NotBlank(message = "Contact number is required.")
	@Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.")
	private String contactNumber;

	@NotBlank(message = "Address is required.")
	@Size(max = 255, message = "Address cannot exceed 255 characters.")
	private String address;

	@Size(max = 2000, message = "Medical history must be under 2000 characters.")
	private String medicalHistory;

	public PatientRegisterRequestDTO() {
	}

	public PatientRegisterRequestDTO(
			@NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") @Size(max = 100, message = "Email cannot exceed 100 characters.") String email,
			@NotBlank(message = "Password is required.") @Size(min = 5, max = 255, message = "Password must be at least 5 characters.") String password,
			@NotBlank(message = "Name is required.") @Size(max = 100, message = "Name cannot exceed 100 characters.") String name,
			@NotNull(message = "Date of Birth is required.") @Past(message = "Date of Birth must be in the past.") LocalDate dob,
			@NotBlank(message = "Gender is required.") Gender gender,
			@NotBlank(message = "Contact number is required.") @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.") String contactNumber,
			@NotBlank(message = "Address is required.") @Size(max = 255, message = "Address cannot exceed 255 characters.") String address,
			@Size(max = 2000, message = "Medical history must be under 2000 characters.") String medicalHistory) {
		super();
		this.email = email;
		this.password = password;
		this.name = name;
		this.dob = dob;
		this.gender = gender;
		this.contactNumber = contactNumber;
		this.address = address;
		this.medicalHistory = medicalHistory;
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

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
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

	public String getMedicalHistory() {
		return medicalHistory;
	}

	public void setMedicalHistory(String medicalHistory) {
		this.medicalHistory = medicalHistory;
	}
}
