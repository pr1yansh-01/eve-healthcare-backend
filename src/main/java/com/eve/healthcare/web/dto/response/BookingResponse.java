package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.Booking;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BookingResponse {

    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("centre_test_id")
    private Long centreTestId;

    @JsonProperty("centre_name")
    private String centreName;

    @JsonProperty("test_name")
    private String testName;

    @JsonProperty("appointment_datetime")
    private OffsetDateTime appointmentDatetime;

    private BigDecimal amount;
    private String status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public BookingResponse() {
    }

    public BookingResponse(Long id, Long userId, Long centreTestId, String centreName, String testName,
                           OffsetDateTime appointmentDatetime, BigDecimal amount, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.centreTestId = centreTestId;
        this.centreName = centreName;
        this.testName = testName;
        this.appointmentDatetime = appointmentDatetime;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static BookingResponse fromBooking(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getCentreTest().getId(),
                booking.getCentreTest().getCentre().getName(),
                booking.getCentreTest().getTest().getName(),
                booking.getAppointmentDatetime(),
                booking.getAmount(),
                booking.getStatus().name(),
                booking.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCentreTestId() {
        return centreTestId;
    }

    public void setCentreTestId(Long centreTestId) {
        this.centreTestId = centreTestId;
    }

    public String getCentreName() {
        return centreName;
    }

    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public OffsetDateTime getAppointmentDatetime() {
        return appointmentDatetime;
    }

    public void setAppointmentDatetime(OffsetDateTime appointmentDatetime) {
        this.appointmentDatetime = appointmentDatetime;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
