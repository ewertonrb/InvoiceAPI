package com.invoice.invoice_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.password-reset")
public class PasswordResetProperties {
    private int expirationMinutes = 30;
    private String frontendBaseUrl = "http://localhost:3000";
    private boolean exposeDevelopmentLink;

    public int getExpirationMinutes() { return expirationMinutes; }
    public void setExpirationMinutes(int expirationMinutes) { this.expirationMinutes = expirationMinutes; }
    public String getFrontendBaseUrl() { return frontendBaseUrl; }
    public void setFrontendBaseUrl(String frontendBaseUrl) { this.frontendBaseUrl = frontendBaseUrl; }
    public boolean isExposeDevelopmentLink() { return exposeDevelopmentLink; }
    public void setExposeDevelopmentLink(boolean exposeDevelopmentLink) { this.exposeDevelopmentLink = exposeDevelopmentLink; }
}
