package com.eve.healthcare.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TestCreateRequest {

    @NotBlank(message = "Test name is required")
    @Size(min = 2, max = 255, message = "Test name must be between 2 and 255 characters")
    private String name;

    private String description;

    public TestCreateRequest() {
    }

    public TestCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}
