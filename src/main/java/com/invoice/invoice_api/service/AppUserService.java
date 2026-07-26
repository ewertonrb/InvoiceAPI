package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.appUser.AppUserPasswordRequestDTO;
import com.invoice.invoice_api.dto.appUser.AppUserRequestDTO;
import com.invoice.invoice_api.dto.appUser.AppUserResponseDTO;
import com.invoice.invoice_api.dto.appUser.AppUserUpdateRequestDTO;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.AppUserMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUserResponseDTO create(AppUserRequestDTO request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException(
                    "An app user already exists with email: "
                            + normalizedEmail
            );
        }

        AppUser appUser = new AppUser();

        appUser.setName(request.name().trim());
        appUser.setEmail(normalizedEmail);
        appUser.setPassword(
                passwordEncoder.encode(request.password())
        );
        appUser.setActive(true);

        AppUser savedAppUser = appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(savedAppUser);
    }

    @Transactional(readOnly = true)
    public AppUserResponseDTO findById(Long id) {
        return AppUserMapper.toResponseDTO(
                findEntityById(id)
        );
    }

    @Transactional(readOnly = true)
    public List<AppUserResponseDTO> findAll() {
        return appUserRepository.findAll()
                .stream()
                .map(AppUserMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public AppUserResponseDTO update(
            Long id,
            AppUserUpdateRequestDTO request
    ) {
        AppUser appUser = findEntityById(id);

        String normalizedEmail = normalizeEmail(request.email());

        appUserRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .filter(existingUser ->
                        !existingUser.getId().equals(id)
                )
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "An app user already exists with email: "
                                    + normalizedEmail
                    );
                });

        appUser.setName(request.name().trim());
        appUser.setEmail(normalizedEmail);

        AppUser updatedAppUser =
                appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(updatedAppUser);
    }

    @Transactional
    public void updatePassword(
            Long id,
            AppUserPasswordRequestDTO request
    ) {
        AppUser appUser = findEntityById(id);

        appUser.setPassword(
                passwordEncoder.encode(request.password())
        );

        appUserRepository.save(appUser);
    }

    @Transactional
    public void deactivate(Long id) {
        AppUser appUser = findEntityById(id);

        if (!Boolean.TRUE.equals(appUser.getActive())) {
            return;
        }

        appUser.setActive(false);
        appUserRepository.save(appUser);
    }

    @Transactional
    public AppUserResponseDTO reactivate(Long id) {
        AppUser appUser = findEntityById(id);

        appUser.setActive(true);

        AppUser reactivatedAppUser =
                appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(
                reactivatedAppUser
        );
    }

    private AppUser findEntityById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App user not found with ID: " + id
                ));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
