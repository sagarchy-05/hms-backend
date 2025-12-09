package com.genc.hms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserLoginRequestDTO {

	@NotBlank(message = "Email is required for login.")
	@Email(message = "Email format is invalid.")
	private String email;

	@NotBlank(message = "Password is required.")
	private String password;

	public UserLoginRequestDTO() {
		//default constructor
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
