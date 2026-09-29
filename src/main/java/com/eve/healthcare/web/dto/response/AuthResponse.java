package com.eve.healthcare.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    private UserResponse user;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType = "bearer";

    public AuthResponse() {
    }

    public AuthResponse(UserResponse user, String accessToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.tokenType = "bearer";
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
