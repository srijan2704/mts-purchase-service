package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.UnitDTO;
import com.mts.mts_purchase_service.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for unit master management.
 */
@RestController
@RequestMapping("/api/units")
@Tag(name = "Units", description = "Manage units of measurement lookup values.")
public class UnitController {

    private final UnitService unitService;

    /** Injects service dependency for unit APIs. */
    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    /** Returns all units. */
    @GetMapping
    @Operation(summary = "List units")
    public ResponseEntity<ApiResponseDTO<List<UnitDTO>>> getAllUnits() {
        return ResponseEntity.ok(ApiResponseDTO.success("Units fetched successfully", unitService.getAllUnits()));
    }

    /** Returns one unit by id. */
    @GetMapping("/{id}")
    @Operation(summary = "Get unit by id")
    public ResponseEntity<ApiResponseDTO<UnitDTO>> getUnitById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Unit fetched successfully", unitService.getUnitById(id)));
    }

    /** Creates one unit. */
    @PostMapping
    @Operation(
            summary = "Create unit",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CreateUnit",
                                    value = """
                                            {
                                              "unitName": "Litre",
                                              "abbreviation": "L",
                                              "description": "Liquid volume unit"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<UnitDTO>> createUnit(@Valid @RequestBody UnitDTO unitDTO) {
        UnitDTO created = unitService.createUnit(unitDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success("Unit created successfully", created));
    }

    /** Updates one unit by id. */
    @PutMapping("/{id}")
    @Operation(summary = "Update unit")
    public ResponseEntity<ApiResponseDTO<UnitDTO>> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UnitDTO unitDTO) {
        UnitDTO updated = unitService.updateUnit(id, unitDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Unit updated successfully", updated));
    }

    /** Deletes one unit by id. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete unit")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Unit deleted successfully", null));
    }
}
