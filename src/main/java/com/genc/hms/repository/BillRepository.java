package com.genc.hms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.genc.hms.entity.Appointment;
import com.genc.hms.entity.Bill;
import com.genc.hms.enums.PaymentStatus;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // Retrieves all bills for a specific patient, ordered by most recent first
    List<Bill> findByPatientPatientIdOrderByBillDateDesc(Long patientId);

    // Retrieves all bills with a specific payment status (e.g., PENDING, PAID)
    List<Bill> findByPaymentStatus(PaymentStatus paymentStatus);

    // Retrieves a bill associated with a specific appointment (ensures unique mapping)
    Optional<Bill> findByAppointment(Appointment appointment);

    // Alternative way to retrieve a bill by appointment ID
    Optional<Bill> findByAppointment_AppointmentId(Long id);
}
