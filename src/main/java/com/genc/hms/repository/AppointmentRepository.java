package com.genc.hms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.genc.hms.entity.Appointment;
import com.genc.hms.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	// Retrieves all appointments for a specific doctor, sorted by date (useful for
	// doctor's schedule view)
	List<Appointment> findByDoctor_DoctorIdOrderByAppointmentDateAsc(Long doctorId);

	// Retrieves all appointments for a specific patient, sorted by date (useful for
	// patient history or dashboard)
	List<Appointment> findByPatient_PatientIdOrderByAppointmentDateAsc(Long patientId);

	// Finds appointments for a doctor on a specific date excluding a particular
	// status (e.g., CANCELLED)
	// Helps identify valid booked slots for availability checks
	List<Appointment> findByDoctorDoctorIdAndAppointmentDateAndStatusNot(Long doctorId, LocalDate appointmentDate,
			AppointmentStatus status);

	// Counts how many active appointments exist for a doctor in a given time slot
	// (excluding cancelled ones)
	// Useful for preventing double-booking of a time slot
	long countByDoctorDoctorIdAndAppointmentDateAndTimeSlotAndStatusNot(Long doctorId, LocalDate date, String timeSlot,
			AppointmentStatus status);
}
