package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodRequest;
import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodResponse;
import cl.ipt.siga.dtos.closing_dto.ClosingResponse;
import cl.ipt.siga.dtos.section_dto.SectionResponse;
import cl.ipt.siga.services.AcademicPeriodClosingService;
import cl.ipt.siga.services.AcademicPeriodService;
import cl.ipt.siga.services.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for academic periods and period closing. No business logic here. */
@RestController
@RequestMapping("/api/academic-periods")
@RequiredArgsConstructor
@Tag(name = "Academic Periods", description = "Academic period management and closing")
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;
    private final AcademicPeriodClosingService closingService;
    private final SectionService sectionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an academic period")
    public ResponseEntity<AcademicPeriodResponse> create(
            @Valid @RequestBody AcademicPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPeriodService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all academic periods")
    public ResponseEntity<List<AcademicPeriodResponse>> findAll() {
        return ResponseEntity.ok(academicPeriodService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an academic period by id")
    public ResponseEntity<AcademicPeriodResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(academicPeriodService.findById(id));
    }

    @GetMapping("/{id}/sections")
    @Operation(summary = "List the sections of an academic period")
    public ResponseEntity<List<SectionResponse>> findSections(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.findByAcademicPeriod(id));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close an academic period")
    public ResponseEntity<ClosingResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(closingService.close(id));
    }
}
