package com.genc.hms.entity;

import java.time.LocalDate;

import com.genc.hms.enums.Gender;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    // Links each patient to a user account (login credentials, role, etc.)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userId_FK", referencedColumnName = "userId", nullable = false)
    private User user;

    @NotBlank(message = "Name is required.")
    @Size(max = 100, message = "Name cannot exceed 100 characters.")
    private String name;

    @NotNull(message = "Date of Birth is required.")
    @Past(message = "Date of Birth must be in the past.")
    private LocalDate dob;

    @NotNull(message = "Gender is required.")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // Validated to ensure exactly 10 digits (numeric contact number)
    @NotBlank(message = "Contact number is required.")
    @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits.")
    @Column(length = 15)
    private String contactNumber;

    @NotBlank(message = "Address is required.")
    @Size(max = 255, message = "Address cannot exceed 255 characters.")
    private String address;

    // Optional field for detailed medical background
    @Size(max = 2000, message = "Medical history must be under 2000 characters.")
    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    public Patient() {
    }

    public Patient(User user, String name, LocalDate dob, Gender gender, String contactNumber, String address,
                   String medicalHistory) {
        this.user = user;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    public Long getPatientId() {
        return patientId;
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

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    @Override
    public String toString() {
        return "Patient [patientId=" + patientId + ", name=" + name + ", dob=" + dob + ", gender=" + gender
                + ", contactNumber=" + contactNumber + ", address=" + address + ", medicalHistory=" + medicalHistory
                + "]";
    }
}
