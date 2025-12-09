package com.genc.hms.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO used by the Patient to request a new appointment booking.
 */
public class AppointmentRequestDTO {

	@NotNull(message = "Doctor ID is required for booking.")
	@Min(value = 1, message = "Doctor ID must be positive.")
	private Long patientId;

	@NotNull(message = "Doctor ID is required for booking.")
	@Min(value = 1, message = "Doctor ID must be positive.")
	private Long doctorId;

	@NotNull(message = "Appointment date is required.")
	@FutureOrPresent(message = "Appointment date must be today or a future date.")
	private LocalDate appointmentDate;

	@NotBlank(message = "Time slot is required.")
	@Size(max = 20, message = "Time slot string exceeds 20 characters.")
	private String timeSlot;

	@NotBlank(message = "Reason for visit is required.")
	@Size(max = 500, message = "Reason cannot exceed 500 characters.")
	private String reason;

	// Constructors
	public AppointmentRequestDTO() {
	}

	public AppointmentRequestDTO(
			@NotNull(message = "Doctor ID is required for booking.") @Min(value = 1, message = "Doctor ID must be positive.") Long patientId,
			@NotNull(message = "Doctor ID is required for booking.") @Min(value = 1, message = "Doctor ID must be positive.") Long doctorId,
			@NotNull(message = "Appointment date is required.") @FutureOrPresent(message = "Appointment date must be today or a future date.") LocalDate appointmentDate,
			@NotBlank(message = "Time slot is required.") @Size(max = 20, message = "Time slot string exceeds 20 characters.") String timeSlot,
			@NotBlank(message = "Reason for visit is required.") @Size(max = 500, message = "Reason cannot exceed 500 characters.") String reason) {
		super();
		this.patientId = patientId;
		this.doctorId = doctorId;
		this.appointmentDate = appointmentDate;
		this.timeSlot = timeSlot;
		this.reason = reason;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDate appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
