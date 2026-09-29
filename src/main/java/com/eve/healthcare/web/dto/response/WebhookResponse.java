package com.eve.healthcare.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookResponse {

    @JsonProperty("event_id")
    private String eventId;

    private String status;
    private String message;

    public WebhookResponse() {
    }

    public WebhookResponse(String eventId, String status, String message) {
        this.eventId = eventId;
        this.status = status;
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
