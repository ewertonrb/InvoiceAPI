package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.appUser.AppUserPasswordRequestDTO;
import com.invoice.invoice_api.dto.appUser.AppUserRequestDTO;
import com.invoice.invoice_api.dto.appUser.AppUserResponseDTO;
import com.invoice.invoice_api.dto.appUser.AppUserUpdateRequestDTO;
import com.invoice.invoice_api.enums.UserStatus;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.AppUserMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

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
        appUser.setSurname(request.surname().trim());
        appUser.setEmail(normalizedEmail);
        appUser.setPassword(
                passwordEncoder.encode(request.password())
        );
        appUser.setStatus(UserStatus.ACTIVE);

        AppUser savedAppUser =
                appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(savedAppUser);
    }

    @Transactional(readOnly = true)
    public AppUserResponseDTO findById(Long id) {

        AppUser appUser = findNonDeletedEntityById(id);

        return AppUserMapper.toResponseDTO(appUser);
    }

    @Transactional(readOnly = true)
    public List<AppUserResponseDTO> findAll() {

        return appUserRepository.findAll()
                .stream()
                .filter(appUser ->
                        appUser.getStatus() != UserStatus.DELETED
                )
                .map(AppUserMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public AppUserResponseDTO update(
            Long id,
            AppUserUpdateRequestDTO request
    ) {
        AppUser appUser = findNonDeletedEntityById(id);

        String normalizedEmail =
                normalizeEmail(request.email());

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
        appUser.setSurname(request.surname().trim());
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
        AppUser appUser = findNonDeletedEntityById(id);

        appUser.setPassword(
                passwordEncoder.encode(request.password())
        );

        appUserRepository.save(appUser);
    }

    @Transactional
    public AppUserResponseDTO block(Long id) {

        AppUser appUser = findEntityByIdIncludingDeleted(id);

        if (appUser.getStatus() == UserStatus.DELETED) {
            throw new InvalidOperationException(
                    "A deleted user cannot be blocked."
            );
        }

        if (appUser.getStatus() == UserStatus.BLOCKED) {
            throw new InvalidOperationException(
                    "User is already blocked."
            );
        }

        appUser.setStatus(UserStatus.BLOCKED);

        AppUser blockedAppUser =
                appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(blockedAppUser);
    }

    @Transactional
    public AppUserResponseDTO unblock(Long id) {

        AppUser appUser = findEntityByIdIncludingDeleted(id);

        if (appUser.getStatus() == UserStatus.DELETED) {
            throw new InvalidOperationException(
                    "A deleted user cannot be unblocked. "
                            + "Use the reactivate operation instead."
            );
        }

        if (appUser.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "User is already active."
            );
        }

        if (appUser.getStatus() != UserStatus.BLOCKED) {
            throw new InvalidOperationException(
                    "Only blocked users can be unblocked."
            );
        }

        appUser.setStatus(UserStatus.ACTIVE);

        AppUser unblockedAppUser =
                appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(unblockedAppUser);
    }

    @Transactional
    public void delete(Long id) {

        AppUser appUser = findEntityByIdIncludingDeleted(id);

        if (appUser.getStatus() == UserStatus.DELETED) {
            return;
        }

        appUser.setStatus(UserStatus.DELETED);

        appUserRepository.save(appUser);
    }

    @Transactional
    public AppUserResponseDTO reactivate(Long id) {

        AppUser appUser = findEntityByIdIncludingDeleted(id);

        if (appUser.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "User is already active."
            );
        }

        if (appUser.getStatus() == UserStatus.BLOCKED) {
            throw new InvalidOperationException(
                    "A blocked user must be unblocked, "
                            + "not reactivated."
            );
        }

        if (appUser.getStatus() != UserStatus.DELETED) {
            throw new InvalidOperationException(
                    "Only deleted users can be reactivated."
            );
        }

        appUser.setStatus(UserStatus.ACTIVE);

        AppUser reactivatedAppUser =
                appUserRepository.save(appUser);

        return AppUserMapper.toResponseDTO(
                reactivatedAppUser
        );
    }

    private AppUser findNonDeletedEntityById(Long id) {

        AppUser appUser =
                findEntityByIdIncludingDeleted(id);

        if (appUser.getStatus() == UserStatus.DELETED) {
            throw new ResourceNotFoundException(
                    "App user not found with ID: " + id
            );
        }

        return appUser;
    }

    private AppUser findEntityByIdIncludingDeleted(Long id) {

        return appUserRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "App user not found with ID: "
                                        + id
                        )
                );
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}