package com.eve.healthcare.web.controller;

import com.eve.healthcare.domain.service.CentreService;
import com.eve.healthcare.web.dto.request.TestCreateRequest;
import com.eve.healthcare.web.dto.response.TestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tests")
@Tag(name = "Diagnostic Tests", description = "Manage and retrieve diagnostic test catalogue")
public class TestController {

    private final CentreService centreService;

    public TestController(CentreService centreService) {
        this.centreService = centreService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "List all available diagnostic tests with optional pagination")
    public ResponseEntity<List<TestResponse>> getAllTests(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(centreService.getAllTests(page, size));
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Create a new diagnostic test in catalogue (Admin/Management operation)")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody TestCreateRequest request) {
        TestResponse response = centreService.createTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
