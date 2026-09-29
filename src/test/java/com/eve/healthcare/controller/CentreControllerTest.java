package com.eve.healthcare.controller;

import com.eve.healthcare.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CentreControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /centres/ - retrieves all diagnostic centres")
    public void testGetAllCentres() throws Exception {
        mockMvc.perform(get("/centres/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", hasItems("HealthFirst Diagnostics", "MedLab Plus", "CityHealth Labs")));
    }

    @Test
    @DisplayName("GET /centres/{id}/ - returns centre details and offered tests with centre-specific prices")
    public void testGetCentreById() throws Exception {
        mockMvc.perform(get("/centres/" + centre1.getId() + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(centre1.getId().intValue())))
                .andExpect(jsonPath("$.name", is("HealthFirst Diagnostics")))
                .andExpect(jsonPath("$.tests", hasSize(4)))
                .andExpect(jsonPath("$.tests[0].price", notNullValue()));
    }

    @Test
    @DisplayName("GET /centres/{id}/ - non-existent centre returns 404 Not Found")
    public void testGetNonExistentCentre() throws Exception {
        mockMvc.perform(get("/centres/99999/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /centres/{id}/tests/ - retrieves tests offered by a centre")
    public void testGetTestsByCentreId() throws Exception {
        mockMvc.perform(get("/centres/" + centre2.getId() + "/tests/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    @DisplayName("Verify price variation: same test has different prices at different centres")
    public void testDifferentPricesForSameTestAcrossCentres() throws Exception {
        // In seed: CBC at Centre 1 is 500.00, CBC at Centre 2 is 450.00
        mockMvc.perform(get("/centres/" + centre1.getId() + "/tests/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.test_name == 'Complete Blood Count (CBC)')].price", hasItem(500.00)));

        mockMvc.perform(get("/centres/" + centre2.getId() + "/tests/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.test_name == 'Complete Blood Count (CBC)')].price", hasItem(450.00)));
    }

    @Test
    @DisplayName("GET /tests/ - retrieves all diagnostic tests in catalogue")
    public void testGetAllTests() throws Exception {
        mockMvc.perform(get("/tests/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].name", hasItem("Complete Blood Count (CBC)")));
    }

    @Test
    @DisplayName("GET /centres/ - pagination returns limited page size")
    public void testGetCentresWithPagination() throws Exception {
        mockMvc.perform(get("/centres/?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("POST /centres/ - unauthenticated access returns 401 Unauthorized")
    public void testCreateCentreUnauthenticated() throws Exception {
        java.util.Map<String, String> body = java.util.Map.of("name", "New Centre", "location", "Pune");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/centres/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /centres/ - authenticated management creates new centre")
    public void testCreateCentreSuccess() throws Exception {
        String token = registerUserAndGetToken("Admin", "admin@example.com", "securepassword123");
        java.util.Map<String, String> body = java.util.Map.of("name", "Apex Diagnostics", "location", "Hyderabad");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/centres/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", org.hamcrest.Matchers.is("Apex Diagnostics")))
                .andExpect(jsonPath("$.location", org.hamcrest.Matchers.is("Hyderabad")));
    }

    @Test
    @DisplayName("POST /tests/ - authenticated management creates new diagnostic test")
    public void testCreateTestSuccess() throws Exception {
        String token = registerUserAndGetToken("Admin", "admin@example.com", "securepassword123");
        java.util.Map<String, String> body = java.util.Map.of(
                "name", "Vitamin D Test",
                "description", "Assesses 25-hydroxy vitamin D levels"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/tests/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", org.hamcrest.Matchers.is("Vitamin D Test")));
    }

    @Test
    @DisplayName("POST /centres/{id}/tests/ - links test to centre with price, rejects duplicates with 409")
    public void testAddTestToCentreAndDuplicateHandling() throws Exception {
        String token = registerUserAndGetToken("Admin", "admin@example.com", "securepassword123");

        // Centre 1 does not currently offer Test 5 (LFT)
        java.util.Map<String, Object> body = java.util.Map.of(
                "test_id", test5.getId(),
                "price", new java.math.BigDecimal("820.00")
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/centres/" + centre1.getId() + "/tests/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price", org.hamcrest.Matchers.is(820.00)));

        // Duplicate addition of same test to same centre should return 409 Conflict
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/centres/" + centre1.getId() + "/tests/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}
