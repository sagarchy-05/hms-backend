package com.genc.hms.entity;

import java.time.LocalTime;

import com.genc.hms.enums.WeekDay;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class DoctorAvailability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long availabilityId;

	// Links this availability slot to a specific doctor
	@NotNull(message = "Doctor association is required.")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctorId_FK", nullable = false)
	private Doctor doctor;

	// Stores which day of the week the doctor is available (e.g., MONDAY)
	@NotNull(message = "Day of week is required.")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WeekDay dayOfWeek;

	// Time when the doctor becomes available
	@NotNull(message = "Start time is required.")
	@Column(nullable = false)
	private LocalTime startTime;

	// Time when the availability ends
	@NotNull(message = "End time is required.")
	@Column(nullable = false)
	private LocalTime endTime;

	public DoctorAvailability() {
	}

	public DoctorAvailability(Doctor doctor, WeekDay dayOfWeek, LocalTime startTime, LocalTime endTime) {
		this.doctor = doctor;
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public Long getAvailabilityId() {
		return availabilityId;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	public WeekDay getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(WeekDay dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "DoctorAvailability [availabilityId=" + availabilityId + ", dayOfWeek=" + dayOfWeek + ", startTime="
				+ startTime + ", endTime=" + endTime + "]";
	}
}
