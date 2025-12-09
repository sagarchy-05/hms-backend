package com.genc.hms.dto;

import java.time.LocalDate;

import com.genc.hms.enums.Gender;

public class PatientResponseDTO {
    private Long patientId;
    private Long userId;
    private String email;
    private String name;
    private LocalDate dob;
    private String contactNumber;
    private String address;
    private Gender gender;
    private String medicalHistory;

    // Constructors
    public PatientResponseDTO() {}

    public PatientResponseDTO(Long patientId, Long userId, String email, String name, LocalDate dob, String contactNumber, String address, Gender gender, String medicalHistory) {
        this.patientId = patientId;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.dob = dob;
        this.contactNumber = contactNumber;
        this.address = address;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
}
