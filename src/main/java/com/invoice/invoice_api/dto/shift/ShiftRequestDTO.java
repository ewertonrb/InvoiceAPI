package com.invoice.invoice_api.dto.shift;
import com.invoice.invoice_api.enums.ShiftMode;
import jakarta.validation.constraints.*;
import java.time.*;
public record ShiftRequestDTO(@NotNull ShiftMode mode,@NotNull Long projectPositionId,Long workerProfileId,@NotNull @FutureOrPresent LocalDate shiftDate,@NotNull LocalTime startTime,@NotNull LocalTime endTime,@NotNull @Min(1) Integer capacity,@Size(max=255) String location,@Size(max=1000) String notes) {}
