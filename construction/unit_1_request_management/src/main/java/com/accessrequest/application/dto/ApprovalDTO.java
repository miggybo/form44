package com.accessrequest.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for approval information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDTO {

    private UUID approvalId;
    private UUID approverId;
    private String approverName;
    private String approvalType;
    private String status;
    private String comments;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant createdAt;
}
