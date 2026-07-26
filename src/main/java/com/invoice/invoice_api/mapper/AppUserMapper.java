package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.appUser.AppUserResponseDTO;
import com.invoice.invoice_api.model.AppUser;

public class AppUserMapper {
    private AppUserMapper() {
    }

    public static AppUserResponseDTO toResponseDTO(AppUser appUser) {
        return new AppUserResponseDTO(
                appUser.getId(),
                appUser.getName(),
                appUser.getSurname(),
                appUser.getEmail(),
                appUser.getActive(),
                appUser.getCreatedAt(),
                appUser.getUpdatedAt()
        );
    }
}