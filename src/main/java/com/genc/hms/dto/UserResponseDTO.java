package com.genc.hms.dto;

public class UserResponseDTO {

	private Long userId;
	private Long roleId;
	private String email;
	private String role;

	public UserResponseDTO() {
	}

	public UserResponseDTO(Long userId, Long roleId, String email, String role) {
		this.userId = userId;
		this.roleId = roleId;
		this.email = email;
		this.role = role;
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
}
