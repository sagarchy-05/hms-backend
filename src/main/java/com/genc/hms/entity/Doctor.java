package com.genc.hms.entity;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    // Each doctor is linked to a user account (for authentication and role-based access)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userId_FK", referencedColumnName = "userId", nullable = false)
    private User user;

    @NotBlank(message = "Doctor name is required.")
    @Size(max = 100, message = "Name cannot exceed 100 characters.")
    private String name;

    @NotBlank(message = "Specialization is required.")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters.")
    private String specialization;

    // Validates doctor’s contact number to exactly 10 digits
    @NotBlank(message = "Contact number is required.")
    @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.")
    @Column(length = 15)
    private String contactNumber;

    // Fee charged per consultation; must be non-negative
    @NotNull(message = "Consultation fee is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee cannot be negative.")
    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    // One doctor can have multiple availability slots (linked via DoctorAvailability)
    @Valid
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorAvailability> doctorAvailabilities;

    public Doctor() {
    }

    public Doctor(User user, String name, String specialization, String contactNumber, BigDecimal consultationFee,
                  List<DoctorAvailability> doctorAvailabilities) {
        this.user = user;
        this.name = name;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.consultationFee = consultationFee;
        this.doctorAvailabilities = doctorAvailabilities;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public List<DoctorAvailability> getDoctorAvailabilities() {
        return doctorAvailabilities;
    }

    public void setDoctorAvailabilities(List<DoctorAvailability> doctorAvailabilities) {
        this.doctorAvailabilities = doctorAvailabilities;
    }

    @Override
    public String toString() {
        return "Doctor [doctorId=" + doctorId + ", name=" + name + ", specialization=" + specialization
                + ", contactNumber=" + contactNumber + ", consultationFee=" + consultationFee
                + ", doctorAvailabilities=" + doctorAvailabilities + "]";
    }
}
