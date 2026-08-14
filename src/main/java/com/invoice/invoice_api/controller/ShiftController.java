package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.shift.ShiftDeclineRequestDTO;
import com.invoice.invoice_api.dto.shift.ShiftRequestDTO;
import com.invoice.invoice_api.dto.shift.ShiftResponseDTO;
import com.invoice.invoice_api.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/shifts")
public class ShiftController {
    private final ShiftService shifts;

    public ShiftController(ShiftService shifts) {
        this.shifts = shifts;
    }

    @PostMapping
    public ResponseEntity<ShiftResponseDTO> create(@PathVariable Long companyId, @Valid @RequestBody ShiftRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shifts.create(companyId, request));
    }

    @GetMapping
    public ResponseEntity<List<ShiftResponseDTO>> list(@PathVariable Long companyId) {
        return ResponseEntity.ok(shifts.listForAdmin(companyId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<ShiftResponseDTO>> available(@PathVariable Long companyId) {
        return ResponseEntity.ok(shifts.available(companyId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ShiftResponseDTO>> mine(@PathVariable Long companyId) {
        return ResponseEntity.ok(shifts.mine(companyId));
    }

    @GetMapping("/{shiftId}")
    public ResponseEntity<ShiftResponseDTO> find(@PathVariable Long companyId, @PathVariable Long shiftId) {
        return ResponseEntity.ok(shifts.find(companyId, shiftId));
    }

    @PostMapping("/{shiftId}/accept")
    public ResponseEntity<ShiftResponseDTO> accept(@PathVariable Long companyId, @PathVariable Long shiftId) {
        return ResponseEntity.ok(shifts.accept(companyId, shiftId));
    }

    @PostMapping("/{shiftId}/decline")
    public ResponseEntity<ShiftResponseDTO> decline(@PathVariable Long companyId, @PathVariable Long shiftId,
                                                     @Valid @RequestBody(required = false) ShiftDeclineRequestDTO request) {
        return ResponseEntity.ok(shifts.decline(companyId, shiftId, request));
    }

    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Void> cancel(@PathVariable Long companyId, @PathVariable Long shiftId) {
        shifts.cancel(companyId, shiftId);
        return ResponseEntity.noContent().build();
    }
}
