package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.appUser.AppUserPasswordRequestDTO;
import com.invoice.invoice_api.dto.appUser.AppUserRequestDTO;
import com.invoice.invoice_api.dto.appUser.AppUserResponseDTO;
import com.invoice.invoice_api.dto.appUser.AppUserUpdateRequestDTO;
import com.invoice.invoice_api.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<AppUserResponseDTO> create(
            @Valid @RequestBody AppUserRequestDTO request
    ) {
        AppUserResponseDTO response =
                appUserService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                appUserService.findById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<AppUserResponseDTO>> findAll() {
        return ResponseEntity.ok(
                appUserService.findAll()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppUserUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(
                appUserService.update(id, request)
        );
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody AppUserPasswordRequestDTO request
    ) {
        appUserService.updatePassword(id, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {
        appUserService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<AppUserResponseDTO> reactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                appUserService.reactivate(id)
        );
    }
}
