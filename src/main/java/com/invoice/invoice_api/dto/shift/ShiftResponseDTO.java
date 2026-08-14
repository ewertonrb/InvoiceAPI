package com.invoice.invoice_api.dto.shift;
import com.invoice.invoice_api.enums.*;
import java.time.*;
import java.util.List;
public record ShiftResponseDTO(Long id, Long companyId, Long projectPositionId, Long projectId, String projectName, String positionName, ShiftMode mode, ShiftStatus status, LocalDate shiftDate, LocalTime startTime, LocalTime endTime, Integer capacity, Integer acceptedCount, Integer remainingSlots, String location, String notes, ShiftAssignmentStatus myAssignmentStatus, List<ShiftAssignmentResponseDTO> assignments, LocalDateTime createdAt, LocalDateTime updatedAt) {}
