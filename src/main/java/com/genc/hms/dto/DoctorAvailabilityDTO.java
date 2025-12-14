package com.genc.hms.dto;

import java.time.LocalTime;

import com.genc.hms.enums.WeekDay;

public class DoctorAvailabilityDTO {
	private WeekDay dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;

	// Constructors
	public DoctorAvailabilityDTO() {
	}

	public DoctorAvailabilityDTO(WeekDay dayOfWeek, LocalTime startTime, LocalTime endTime) {
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	// Getters and Setters
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
}
