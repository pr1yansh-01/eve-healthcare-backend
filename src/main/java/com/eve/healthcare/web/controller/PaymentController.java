package com.eve.healthcare.web.controller;

import com.eve.healthcare.common.security.SecurityUtils;
import com.eve.healthcare.domain.service.PaymentService;
import com.eve.healthcare.domain.service.WebhookService;
import com.eve.healthcare.web.dto.request.PaymentCreateRequest;
import com.eve.healthcare.web.dto.request.WebhookRequest;
import com.eve.healthcare.web.dto.response.PaymentResponse;
import com.eve.healthcare.web.dto.response.WebhookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Simulated payments and idempotent webhook processing")
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookService webhookService;

    public PaymentController(PaymentService paymentService, WebhookService webhookService) {
        this.paymentService = paymentService;
        this.webhookService = webhookService;
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Initiate a simulated payment for a booking")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PaymentResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping({"/webhook", "/webhook/"})
    @Operation(summary = "Receive simulated payment status update webhook (Idempotent)")
    public ResponseEntity<WebhookResponse> receiveWebhook(@Valid @RequestBody WebhookRequest request) {
        WebhookResponse response = webhookService.processWebhook(request);
        return ResponseEntity.ok(response);
    }
}
