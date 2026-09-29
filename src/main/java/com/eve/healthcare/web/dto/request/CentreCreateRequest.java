package com.eve.healthcare.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CentreCreateRequest {

    @NotBlank(message = "Centre name is required")
    @Size(min = 2, max = 255, message = "Centre name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 500, message = "Location must be between 2 and 500 characters")
    private String location;

    public CentreCreateRequest() {
    }

    public CentreCreateRequest(String name, String location) {
        this.name = name;
        this.location = location;
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
}
