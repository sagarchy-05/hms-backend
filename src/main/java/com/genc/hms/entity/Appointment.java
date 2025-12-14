package com.genc.hms.entity;

import java.time.LocalDate;
import com.genc.hms.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "appointments")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long appointmentId;

	// Each appointment is linked to one patient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patientId", nullable = false)
	private Patient patient;

	// Each appointment is linked to one doctor
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctorId", nullable = false)
	private Doctor doctor;

	// Date on which the appointment is scheduled
	@Column(nullable = false)
	private LocalDate appointmentDate;

	// Specific time slot for the appointment (e.g., "10:00 AM - 10:30 AM")
	@Column(length = 20, nullable = false)
	private String timeSlot;

	// Short reason or purpose of visit (stored up to 500 characters)
	@Size(max = 500, message = "Reason cannot exceed 500 characters.")
	@Column(length = 500, nullable = false)
	private String reason;

	// Current status of the appointment (e.g., BOOKED, COMPLETED, CANCELLED)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AppointmentStatus status;

	// Additional notes or remarks added by doctor or staff
	@Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
	private String remarks;

	public Appointment() {
	}

	public Appointment(Patient patient, Doctor doctor, LocalDate appointmentDate, String timeSlot, String reason,
			AppointmentStatus status, String remarks) {
		this.patient = patient;
		this.doctor = doctor;
		this.appointmentDate = appointmentDate;
		this.timeSlot = timeSlot;
		this.reason = reason;
		this.status = status;
		this.remarks = remarks;
	}

	public Long getAppointmentId() {
		return appointmentId;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
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

	@Override
	public String toString() {
		return "Appointment [appointmentId=" + appointmentId + ", appointmentDate=" + appointmentDate + ", timeSlot="
				+ timeSlot + ", reason=" + reason + ", status=" + status + ", remarks=" + remarks + "]";
	}
}
