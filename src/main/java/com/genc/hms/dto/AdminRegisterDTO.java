package com.genc.hms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminRegisterDTO {
	@NotBlank(message = "Email is required.")
	@Email(message = "Email format is invalid.")
	@Size(max = 100, message = "Email cannot exceed 100 characters.")
	private String email;
	
	@NotBlank(message = "Password is required.")
	@Size(min = 5, max = 255, message = "Password must be at least 5 characters.")
	private String password;

	public AdminRegisterDTO() {
		super();
	}

	public AdminRegisterDTO(
			@NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") @Size(max = 100, message = "Email cannot exceed 100 characters.") String email,
			@NotBlank(message = "Password is required.") @Size(min = 5, max = 255, message = "Password must be at least 5 characters.") String password) {
		super();
		this.email = email;
		this.password = password;
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

}
