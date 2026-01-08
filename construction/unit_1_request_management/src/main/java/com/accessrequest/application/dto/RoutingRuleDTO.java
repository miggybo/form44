package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for access type routing rule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRuleDTO {

    private UUID routingId;
    private String administratorRole;
    private Boolean isDefault;
}
