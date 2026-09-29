package com.eve.healthcare.web.dto.response;

import com.eve.healthcare.domain.model.CentreTest;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CentreTestResponse {

    private Long id;

    @JsonProperty("test_id")
    private Long testId;

    @JsonProperty("test_name")
    private String testName;

    private BigDecimal price;

    public CentreTestResponse() {
    }

    public CentreTestResponse(Long id, Long testId, String testName, BigDecimal price) {
        this.id = id;
        this.testId = testId;
        this.testName = testName;
        this.price = price;
    }

    public static CentreTestResponse fromCentreTest(CentreTest ct) {
        return new CentreTestResponse(
                ct.getId(),
                ct.getTest().getId(),
                ct.getTest().getName(),
                ct.getPrice()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
