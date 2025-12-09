package com.genc.hms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO returned to the client upon successful login. It contains the generated
 * JWT token and key user information.
 */
public class JwtAuthenticationResponseDTO {

	private String token;

	// User details extracted from the authenticated principal
	private Long userId;
	private Long roleId; // The ID of the specific role profile (e.g., Patient ID or Doctor ID)
	private String email;
	private String role; // The name of the role (e.g., "ADMIN", "DOCTOR", "PATIENT")

	public JwtAuthenticationResponseDTO() {
	}

	public JwtAuthenticationResponseDTO(String token, Long userId, Long roleId, String email, String role) {
		this.token = token;
		this.userId = userId;
		this.roleId = roleId;
		this.email = email;
		this.role = role;
	}

	// Standard Getters and Setters

	@JsonProperty("access_token") // Good practice to name the JWT field explicitly for client-side use
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "JwtAuthenticationResponseDTO [userId=" + userId + ", email=" + email + ", role=" + role
				+ ", token=***]";
	}
}