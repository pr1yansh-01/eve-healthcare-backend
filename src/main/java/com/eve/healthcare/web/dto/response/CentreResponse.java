package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.DiagnosticCentre;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class CentreResponse {

    private Long id;
    private String name;
    private String location;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public CentreResponse() {
    }

    public CentreResponse(Long id, String name, String location, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
    }

    public static CentreResponse fromCentre(DiagnosticCentre centre) {
        return new CentreResponse(centre.getId(), centre.getName(), centre.getLocation(), centre.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
