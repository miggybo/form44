package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO for declining a request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeclineRequestRequest {

    @NotNull(message = "Decliner ID is required")
    private UUID declinerId;

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 5000, message = "Reason must be between 10 and 5000 characters")
    private String reason;
}
