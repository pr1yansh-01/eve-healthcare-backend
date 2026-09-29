package com.eve.healthcare.web.controller;

import com.eve.healthcare.domain.service.CentreService;
import com.eve.healthcare.web.dto.request.CentreCreateRequest;
import com.eve.healthcare.web.dto.request.CentreTestCreateRequest;
import com.eve.healthcare.web.dto.response.CentreDetailResponse;
import com.eve.healthcare.web.dto.response.CentreResponse;
import com.eve.healthcare.web.dto.response.CentreTestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/centres")
@Tag(name = "Diagnostic Centres", description = "Manage and retrieve diagnostic centres and available test offerings")
public class CentreController {

    private final CentreService centreService;

    public CentreController(CentreService centreService) {
        this.centreService = centreService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "List all diagnostic centres with optional pagination")
    public ResponseEntity<List<CentreResponse>> getAllCentres(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(centreService.getAllCentres(page, size));
    }

    @GetMapping({"/{centre_id}", "/{centre_id}/"})
    @Operation(summary = "Get diagnostic centre details with all offered tests and prices")
    public ResponseEntity<CentreDetailResponse> getCentreById(@PathVariable("centre_id") Long centreId) {
        return ResponseEntity.ok(centreService.getCentreById(centreId));
    }

    @GetMapping({"/{centre_id}/tests", "/{centre_id}/tests/"})
    @Operation(summary = "List diagnostic tests offered by a specific centre with optional pagination")
    public ResponseEntity<List<CentreTestResponse>> getTestsByCentreId(
            @PathVariable("centre_id") Long centreId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(centreService.getTestsByCentreId(centreId, page, size));
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Create a new diagnostic centre (Admin/Management operation)")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<CentreResponse> createCentre(@Valid @RequestBody CentreCreateRequest request) {
        CentreResponse response = centreService.createCentre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping({"/{centre_id}/tests", "/{centre_id}/tests/"})
    @Operation(summary = "Add a diagnostic test with specific price to a centre (Admin/Management operation)")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<CentreTestResponse> addTestToCentre(
            @PathVariable("centre_id") Long centreId,
            @Valid @RequestBody CentreTestCreateRequest request) {
        CentreTestResponse response = centreService.addTestToCentre(centreId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
