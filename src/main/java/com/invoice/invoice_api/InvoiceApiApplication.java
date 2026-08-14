package com.invoice.invoice_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        com.invoice.invoice_api.config.NotificationProperties.class,
        com.invoice.invoice_api.config.PasswordResetProperties.class
})
public class InvoiceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoiceApiApplication.class, args);
	}

}
