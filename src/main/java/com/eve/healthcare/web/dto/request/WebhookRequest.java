package com.eve.healthcare.web.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class WebhookRequest {

    @NotBlank(message = "Event ID is required")
    @JsonProperty("event_id")
    @JsonAlias({"eventId", "event_id"})
    private String eventId;

    @NotNull(message = "Payment ID is required")
    @Positive(message = "Payment ID must be positive")
    @JsonProperty("payment_id")
    @JsonAlias({"paymentId", "payment_id"})
    private Long paymentId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(SUCCESS|FAILED)$", message = "Status must be either SUCCESS or FAILED")
    private String status;

    public WebhookRequest() {
    }

    public WebhookRequest(String eventId, Long paymentId, String status) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
