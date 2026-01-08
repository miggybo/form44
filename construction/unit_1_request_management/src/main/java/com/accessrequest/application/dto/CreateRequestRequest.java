package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequestRequest {

    @NotNull(message = "Requestor ID is required")
    private UUID requestorId;

    @NotBlank(message = "Access type is required")
    private String accessType;

    @NotBlank(message = "System name is required")
    @Size(min = 1, max = 255, message = "System name must be between 1 and 255 characters")
    private String systemName;

    @NotBlank(message = "Justification is required")
    @Size(min = 10, max = 10485760, message = "Justification must be between 10 and 10MB")
    private String justification;

    private List<UUID> documentIds;
}
