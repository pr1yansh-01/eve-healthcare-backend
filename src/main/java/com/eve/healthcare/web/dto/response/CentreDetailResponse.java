package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.DiagnosticCentre;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public class CentreDetailResponse {

    private Long id;
    private String name;
    private String location;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private List<CentreTestResponse> tests;

    public CentreDetailResponse() {
    }

    public CentreDetailResponse(Long id, String name, String location, OffsetDateTime createdAt, List<CentreTestResponse> tests) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
        this.tests = tests;
    }

    public static CentreDetailResponse fromCentre(DiagnosticCentre centre) {
        List<CentreTestResponse> testResponses = centre.getCentreTests().stream()
                .map(CentreTestResponse::fromCentreTest)
                .toList();

        return new CentreDetailResponse(
                centre.getId(),
                centre.getName(),
                centre.getLocation(),
                centre.getCreatedAt(),
                testResponses
        );
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

    public List<CentreTestResponse> getTests() {
        return tests;
    }

    public void setTests(List<CentreTestResponse> tests) {
        this.tests = tests;
    }
}
