package com.eve.healthcare.controller;

import com.eve.healthcare.BaseIntegrationTest;
import com.eve.healthcare.common.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("POST /auth/signup/ - successfully registers new user and issues JWT")
    public void testSignupSuccess() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Alice",
                "email", "alice@example.com",
                "password", "strongpassword123"
        );

        mockMvc.perform(post("/auth/signup/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name", is("Alice")))
                .andExpect(jsonPath("$.user.email", is("alice@example.com")))
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.token_type", is("bearer")))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
    }

    @Test
    @DisplayName("POST /auth/signup/ - duplicate email returns 409 Conflict")
    public void testSignupDuplicateEmail() throws Exception {
        registerUserAndGetToken("Alice", "alice@example.com", "strongpassword123");

        Map<String, String> body = Map.of(
                "name", "Alice Clone",
                "email", "alice@example.com",
                "password", "anotherpassword123"
        );

        mockMvc.perform(post("/auth/signup/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("POST /auth/signup/ - short password returns 422 Unprocessable Entity")
    public void testSignupShortPassword() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Alice",
                "email", "alice@example.com",
                "password", "short"
        );

        mockMvc.perform(post("/auth/signup/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /auth/signup/ - invalid email format returns 422")
    public void testSignupInvalidEmail() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Alice",
                "email", "not-an-email",
                "password", "strongpassword123"
        );

        mockMvc.perform(post("/auth/signup/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /auth/login/ - successful login returns access token")
    public void testLoginSuccess() throws Exception {
        registerUserAndGetToken("Bob", "bob@example.com", "securepassword123");

        Map<String, String> loginBody = Map.of(
                "email", "bob@example.com",
                "password", "securepassword123"
        );

        mockMvc.perform(post("/auth/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("bob@example.com")))
                .andExpect(jsonPath("$.access_token", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/login/ - wrong password returns 401 Unauthorized")
    public void testLoginWrongPassword() throws Exception {
        registerUserAndGetToken("Bob", "bob@example.com", "securepassword123");

        Map<String, String> loginBody = Map.of(
                "email", "bob@example.com",
                "password", "wrongpassword"
        );

        mockMvc.perform(post("/auth/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login/ - non-existent email returns 401 Unauthorized")
    public void testLoginNonExistentEmail() throws Exception {
        Map<String, String> loginBody = Map.of(
                "email", "nobody@example.com",
                "password", "securepassword123"
        );

        mockMvc.perform(post("/auth/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /bookings/ - unauthenticated access returns 401 Unauthorized")
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/bookings/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /bookings/ - invalid Bearer token returns 401 Unauthorized")
    public void testInvalidTokenAccess() throws Exception {
        mockMvc.perform(get("/bookings/")
                        .header("Authorization", "Bearer invalid.malformed.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /bookings/ - expired Bearer token returns 401 Unauthorized")
    public void testExpiredTokenAccess() throws Exception {
        String token = registerUserAndGetToken("Eve", "eve@example.com", "securepassword123");
        // Generate an immediately expired token for the same user ID
        String expiredToken = jwtService.generateTokenWithCustomExpiry(1L, "eve@example.com", -1000L);

        mockMvc.perform(get("/bookings/")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
