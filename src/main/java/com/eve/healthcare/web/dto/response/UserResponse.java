package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class UserResponse {

    private Long id;
    private String name;
    private String email;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public static UserResponse fromUser(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
