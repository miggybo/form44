package com.accessrequest.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for request history entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryDTO {

    private UUID historyId;
    private String action;
    private UUID actorId;
    private String actorName;
    private String previousStatus;
    private String newStatus;
    private String comments;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant createdAt;
}
