package com.genc.hms.dto;

import java.math.BigDecimal;
import java.util.List;

public class DoctorResponseDTO {
    private Long doctorId;
    private String name;
    private String specialization;
    private String contactNumber;
    private BigDecimal consultationFee;
    private List<DoctorAvailabilityDTO> availabilities;
    
    // Constructors
    public DoctorResponseDTO() {}

    public DoctorResponseDTO(Long doctorId, String name, String specialization, String contactNumber, BigDecimal consultationFee, List<DoctorAvailabilityDTO> availabilities) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.consultationFee = consultationFee;
        this.availabilities = availabilities;
    }

    // Getters and Setters
    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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

    public List<DoctorAvailabilityDTO> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<DoctorAvailabilityDTO> availabilities) {
        this.availabilities = availabilities;
    }
}
