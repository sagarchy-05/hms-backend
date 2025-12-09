package com.genc.hms.dto;

import com.genc.hms.enums.Role;

import jakarta.validation.constraints.Email;
/**
 * DTO used by the Admin to update a User's email and/or role.
 * Both fields are optional for partial updates.
 */
public class UserUpdateRoleAndEmailDTO {

    /**
     * The new email address for the user.
     * Only validated if present, allowing the field to be null for role-only updates.
     */
    @Email(message = "Email must be a valid email address.")
    private String email; 

    /**
     * The new role (ADMIN, DOCTOR, PATIENT) for the user.
     * Can be null if only the email is being updated.
     */
    private Role role;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
    
    
}
