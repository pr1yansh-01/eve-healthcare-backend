package com.eve.healthcare;

import com.eve.healthcare.domain.model.CentreTest;
import com.eve.healthcare.domain.model.DiagnosticCentre;
import com.eve.healthcare.domain.model.DiagnosticTest;
import com.eve.healthcare.domain.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected DiagnosticCentreRepository centreRepository;

    @Autowired
    protected DiagnosticTestRepository testRepository;

    @Autowired
    protected CentreTestRepository centreTestRepository;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected PaymentRepository paymentRepository;

    @Autowired
    protected WebhookEventRepository webhookEventRepository;

    protected DiagnosticCentre centre1;
    protected DiagnosticCentre centre2;
    protected DiagnosticCentre centre3;
    protected DiagnosticTest test1;
    protected DiagnosticTest test2;
    protected DiagnosticTest test3;
    protected DiagnosticTest test4;
    protected DiagnosticTest test5;
    protected CentreTest ct1_1;
    protected CentreTest ct1_2;
    protected CentreTest ct2_1;

    @BeforeEach
    public void setupDatabase() {
        webhookEventRepository.deleteAll();
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        centreTestRepository.deleteAll();
        testRepository.deleteAll();
        centreRepository.deleteAll();
        userRepository.deleteAll();

        // Seed Centres
        centre1 = centreRepository.save(new DiagnosticCentre("HealthFirst Diagnostics", "Mumbai, Maharashtra"));
        centre2 = centreRepository.save(new DiagnosticCentre("MedLab Plus", "Delhi, NCR"));
        centre3 = centreRepository.save(new DiagnosticCentre("CityHealth Labs", "Bangalore, Karnataka"));

        // Seed Tests
        test1 = testRepository.save(new DiagnosticTest("Complete Blood Count (CBC)", "Measures blood components"));
        test2 = testRepository.save(new DiagnosticTest("Lipid Profile", "Measures cholesterol"));
        test3 = testRepository.save(new DiagnosticTest("Thyroid Panel (TSH)", "Measures thyroid hormone"));
        test4 = testRepository.save(new DiagnosticTest("Blood Glucose Fasting", "Measures blood sugar"));
        test5 = testRepository.save(new DiagnosticTest("Liver Function Test (LFT)", "Evaluates liver health"));

        // Seed Centre-Tests (Centre 1: CBC is 500, Lipid is 800; Centre 2: CBC is 450)
        ct1_1 = centreTestRepository.save(new CentreTest(centre1, test1, new BigDecimal("500.00")));
        ct1_2 = centreTestRepository.save(new CentreTest(centre1, test2, new BigDecimal("800.00")));
        centreTestRepository.save(new CentreTest(centre1, test3, new BigDecimal("600.00")));
        centreTestRepository.save(new CentreTest(centre1, test4, new BigDecimal("200.00")));

        ct2_1 = centreTestRepository.save(new CentreTest(centre2, test1, new BigDecimal("450.00")));
        centreTestRepository.save(new CentreTest(centre2, test2, new BigDecimal("750.00")));
        centreTestRepository.save(new CentreTest(centre2, test4, new BigDecimal("180.00")));
        centreTestRepository.save(new CentreTest(centre2, test5, new BigDecimal("900.00")));

        centreTestRepository.save(new CentreTest(centre3, test1, new BigDecimal("550.00")));
        centreTestRepository.save(new CentreTest(centre3, test3, new BigDecimal("650.00")));
        centreTestRepository.save(new CentreTest(centre3, test5, new BigDecimal("850.00")));
    }

    protected String registerUserAndGetToken(String name, String email, String password) throws Exception {
        Map<String, String> body = Map.of(
                "name", name,
                "email", email,
                "password", password
        );

        MvcResult result = mockMvc.perform(post("/auth/signup/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (String) map.get("access_token");
    }
}
