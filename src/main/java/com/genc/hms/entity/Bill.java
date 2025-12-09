package com.genc.hms.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.genc.hms.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    // Each bill is uniquely linked to a single appointment
    // Cascade ensures that appointment-related billing updates propagate automatically
    @NotNull(message = "Bill must be associated with an appointment.")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "appointmentId", nullable = false, unique = true)
    private Appointment appointment;

    // Multiple bills can be associated with one patient (e.g., multiple visits)
    @NotNull(message = "Patient record is required for billing.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientId", nullable = false)
    private Patient patient;

    // Total charge amount for this bill, stored with 2 decimal precision
    @NotNull(message = "Bill amount is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Bill amount cannot be negative.")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal billAmount;

    // Indicates if payment is PENDING, COMPLETED, or FAILED
    @NotNull(message = "Payment status is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    // Date when the bill was issued; restricted to present or past to ensure data integrity
    @NotNull(message = "Bill date is required.")
    @PastOrPresent(message = "Bill date cannot be in the future.")
    @Column(nullable = false)
    private LocalDate billDate;

    public Bill() {
    }

    public Bill(Appointment appointment, Patient patient, BigDecimal billAmount,
                PaymentStatus paymentStatus, LocalDate billDate) {
        this.appointment = appointment;
        this.patient = patient;
        this.billAmount = billAmount;
        this.paymentStatus = paymentStatus;
        this.billDate = billDate;
    }

    public Long getBillId() {
        return billId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public BigDecimal getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(BigDecimal billAmount) {
        this.billAmount = billAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    @Override
    public String toString() {
        return "Bill [billId=" + billId + ", appointment=" + appointment
                + ", billAmount=" + billAmount + ", paymentStatus=" + paymentStatus
                + ", billDate=" + billDate + "]";
    }
}
