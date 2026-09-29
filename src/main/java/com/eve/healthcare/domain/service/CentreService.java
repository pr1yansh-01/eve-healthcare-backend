package com.eve.healthcare.domain.service;

import com.eve.healthcare.common.exception.ConflictException;
import com.eve.healthcare.common.exception.ResourceNotFoundException;
import com.eve.healthcare.domain.model.CentreTest;
import com.eve.healthcare.domain.model.DiagnosticCentre;
import com.eve.healthcare.domain.model.DiagnosticTest;
import com.eve.healthcare.domain.repository.CentreTestRepository;
import com.eve.healthcare.domain.repository.DiagnosticCentreRepository;
import com.eve.healthcare.domain.repository.DiagnosticTestRepository;
import com.eve.healthcare.web.dto.request.CentreCreateRequest;
import com.eve.healthcare.web.dto.request.CentreTestCreateRequest;
import com.eve.healthcare.web.dto.request.TestCreateRequest;
import com.eve.healthcare.web.dto.response.CentreDetailResponse;
import com.eve.healthcare.web.dto.response.CentreResponse;
import com.eve.healthcare.web.dto.response.CentreTestResponse;
import com.eve.healthcare.web.dto.response.TestResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CentreService {

    private final DiagnosticCentreRepository centreRepository;
    private final DiagnosticTestRepository testRepository;
    private final CentreTestRepository centreTestRepository;

    public CentreService(
            DiagnosticCentreRepository centreRepository,
            DiagnosticTestRepository testRepository,
            CentreTestRepository centreTestRepository) {
        this.centreRepository = centreRepository;
        this.testRepository = testRepository;
        this.centreTestRepository = centreTestRepository;
    }

    @Transactional(readOnly = true)
    public List<CentreResponse> getAllCentres(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return centreRepository.findAll(pageable).stream()
                .map(CentreResponse::fromCentre)
                .toList();
    }

    @Transactional(readOnly = true)
    public CentreDetailResponse getCentreById(Long id) {
        DiagnosticCentre centre = centreRepository.findByIdWithTests(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centre not found with ID: " + id));

        return CentreDetailResponse.fromCentre(centre);
    }

    @Transactional(readOnly = true)
    public List<CentreTestResponse> getTestsByCentreId(Long centreId, int page, int size) {
        if (!centreRepository.existsById(centreId)) {
            throw new ResourceNotFoundException("Centre not found with ID: " + centreId);
        }

        List<CentreTest> offerings = centreTestRepository.findByCentreIdWithTest(centreId);
        int fromIndex = Math.min(page * size, offerings.size());
        int toIndex = Math.min(fromIndex + size, offerings.size());

        return offerings.subList(fromIndex, toIndex).stream()
                .map(CentreTestResponse::fromCentreTest)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TestResponse> getAllTests(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return testRepository.findAll(pageable).stream()
                .map(TestResponse::fromTest)
                .toList();
    }

    @Transactional
    public CentreResponse createCentre(CentreCreateRequest request) {
        DiagnosticCentre centre = new DiagnosticCentre(request.getName(), request.getLocation());
        DiagnosticCentre saved = centreRepository.save(centre);
        return CentreResponse.fromCentre(saved);
    }

    @Transactional
    public TestResponse createTest(TestCreateRequest request) {
        DiagnosticTest test = new DiagnosticTest(request.getName(), request.getDescription());
        DiagnosticTest saved = testRepository.save(test);
        return TestResponse.fromTest(saved);
    }

    @Transactional
    public CentreTestResponse addTestToCentre(Long centreId, CentreTestCreateRequest request) {
        DiagnosticCentre centre = centreRepository.findById(centreId)
                .orElseThrow(() -> new ResourceNotFoundException("Centre not found with ID: " + centreId));

        DiagnosticTest test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with ID: " + request.getTestId()));

        if (centreTestRepository.findByCentreIdAndTestId(centreId, request.getTestId()).isPresent()) {
            throw new ConflictException("Test with ID " + request.getTestId() + " is already offered by this centre");
        }

        CentreTest centreTest = new CentreTest(centre, test, request.getPrice());
        CentreTest saved = centreTestRepository.save(centreTest);
        return CentreTestResponse.fromCentreTest(saved);
    }
}
