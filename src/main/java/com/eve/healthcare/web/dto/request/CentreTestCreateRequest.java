package com.eve.healthcare.web.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CentreTestCreateRequest {

    @NotNull(message = "Test ID is required")
    @Positive(message = "Test ID must be positive")
    @JsonProperty("test_id")
    @JsonAlias({"testId", "test_id"})
    private Long testId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Price format must have up to 8 integer digits and 2 decimal places")
    private BigDecimal price;

    public CentreTestCreateRequest() {
    }

    public CentreTestCreateRequest(Long testId, BigDecimal price) {
        this.testId = testId;
        this.price = price;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
