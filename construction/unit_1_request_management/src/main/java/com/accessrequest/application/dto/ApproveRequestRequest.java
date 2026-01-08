package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for approving a request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequestRequest {

    @NotNull(message = "Approver ID is required")
    private UUID approverId;

    private String comments;
}
