package com.accessrequest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for configuring access type routing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigureRoutingRequest {

    @NotNull(message = "Administrator roles are required")
    private List<String> administratorRoles;

    @NotBlank(message = "Default role is required")
    private String defaultRole;
}
