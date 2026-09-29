package com.eve.healthcare.web.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public class BookingCreateRequest {

    @NotNull(message = "Centre ID is required")
    @Positive(message = "Centre ID must be positive")
    @JsonProperty("centre_id")
    @JsonAlias({"centreId", "centre_id"})
    private Long centreId;

    @NotNull(message = "Test ID is required")
    @Positive(message = "Test ID must be positive")
    @JsonProperty("test_id")
    @JsonAlias({"testId", "test_id"})
    private Long testId;

    @NotNull(message = "Appointment datetime is required")
    @Future(message = "Appointment datetime must be in the future")
    @JsonProperty("appointment_datetime")
    @JsonAlias({"appointmentDatetime", "appointment_datetime"})
    private OffsetDateTime appointmentDatetime;

    public BookingCreateRequest() {
    }

    public BookingCreateRequest(Long centreId, Long testId, OffsetDateTime appointmentDatetime) {
        this.centreId = centreId;
        this.testId = testId;
        this.appointmentDatetime = appointmentDatetime;
    }

    public Long getCentreId() {
        return centreId;
    }

    public void setCentreId(Long centreId) {
        this.centreId = centreId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public OffsetDateTime getAppointmentDatetime() {
        return appointmentDatetime;
    }

    public void setAppointmentDatetime(OffsetDateTime appointmentDatetime) {
        this.appointmentDatetime = appointmentDatetime;
    }
}
