package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for endorsing a request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndorseRequestRequest {

    @NotNull(message = "Reviewer ID is required")
    private UUID reviewerId;

    private String comments;
}
