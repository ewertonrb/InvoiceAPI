package com.invoice.invoice_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class InvitationProperties {
    private final Frontend frontend =
            new Frontend();

    private final Invitation invitation =
            new Invitation();

    public Frontend getFrontend() {
        return frontend;
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public static class Frontend {

        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Invitation {

        private long expirationDays = 7;

        public long getExpirationDays() {
            return expirationDays;
        }

        public void setExpirationDays(
                long expirationDays
        ) {
            this.expirationDays =
                    expirationDays;
        }
    }
}
