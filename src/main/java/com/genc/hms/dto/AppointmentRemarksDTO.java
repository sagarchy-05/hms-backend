package com.genc.hms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class AppointmentRemarksDTO {

	@NotEmpty(message = "Remarks cannot be empty.")
	@Size(max = 2000, message = "Remarks must be less than 2000 characters.")
	private String remarks;

	// Getters and Setters
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
