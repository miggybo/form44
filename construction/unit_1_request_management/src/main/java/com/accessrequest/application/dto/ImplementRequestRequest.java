package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for marking a request as implemented.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImplementRequestRequest {

    @NotNull(message = "Implementer ID is required")
    private UUID implementerId;

    private String notes;
}
