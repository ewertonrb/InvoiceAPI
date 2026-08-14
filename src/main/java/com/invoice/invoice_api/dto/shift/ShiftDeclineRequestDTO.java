package com.invoice.invoice_api.dto.shift;
import jakarta.validation.constraints.Size;
public record ShiftDeclineRequestDTO(@Size(max=500) String reason) {}
