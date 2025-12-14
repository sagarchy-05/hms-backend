package com.genc.hms.dto;

import java.time.LocalDate;

import com.genc.hms.enums.AppointmentStatus;

/**
 * DTO used to represent appointment details when returning data to the user or
 * admin.
 */
public class AppointmentResponseDTO {

	private Long appointmentId;

	// Patient Details
	private Long patientId;
	private String patientName;

	// Doctor Details
	private Long doctorId;
	private String doctorName;
	private String specialization;

	private LocalDate appointmentDate;
	private String timeSlot;
	private String reason;
	private AppointmentStatus status;
	private String remarks;

	// Constructors
	public AppointmentResponseDTO() {
	}

	public AppointmentResponseDTO(Long appointmentId, Long patientId, String patientName, Long doctorId,
			String doctorName, String specialization, LocalDate appointmentDate, String timeSlot, String reason,
			AppointmentStatus status, String remarks) {
		this.appointmentId = appointmentId;
		this.patientId = patientId;
		this.patientName = patientName;
		this.doctorId = doctorId;
		this.doctorName = doctorName;
		this.specialization = specialization;
		this.appointmentDate = appointmentDate;
		this.timeSlot = timeSlot;
		this.reason = reason;
		this.status = status;
		this.remarks = remarks;
	}

	// Getters and Setters
	public Long getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Long appointmentId) {
		this.appointmentId = appointmentId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
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

	public AppointmentStatus getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
