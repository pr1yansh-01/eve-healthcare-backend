package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.Payment;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentResponse {

    private Long id;

    @JsonProperty("booking_id")
    private Long bookingId;

    private BigDecimal amount;
    private String status;

    @JsonProperty("provider_reference")
    private String providerReference;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id, Long bookingId, BigDecimal amount, String status, String providerReference, OffsetDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.providerReference = providerReference;
        this.createdAt = createdAt;
    }

    public static PaymentResponse fromPayment(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getProviderReference(),
                payment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
