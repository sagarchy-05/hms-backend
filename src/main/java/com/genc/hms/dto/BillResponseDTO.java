package com.genc.hms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.genc.hms.enums.PaymentStatus;

/**
 * Data Transfer Object for responding with Bill details.
 */
public record BillResponseDTO(
    Long billId,
    Long patientId,
    String patientName,
    Long appointmentId,
    BigDecimal billAmount,
    PaymentStatus paymentStatus,
    LocalDate billDate
) {}
