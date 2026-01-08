package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for returning a request to reviewer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequestRequest {

    @NotNull(message = "Returner ID is required")
    private UUID returnerId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
