package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.DiagnosticTest;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class TestResponse {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public TestResponse() {
    }

    public TestResponse(Long id, String name, String description, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static TestResponse fromTest(DiagnosticTest test) {
        return new TestResponse(test.getId(), test.getName(), test.getDescription(), test.getCreatedAt());
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
