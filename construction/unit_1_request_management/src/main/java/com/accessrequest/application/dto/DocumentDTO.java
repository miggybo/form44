package com.accessrequest.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for document information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {

    private UUID documentId;
    private String fileName;
    private Long fileSize;
    private String contentType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant uploadedAt;

    private UUID uploadedBy;
}
