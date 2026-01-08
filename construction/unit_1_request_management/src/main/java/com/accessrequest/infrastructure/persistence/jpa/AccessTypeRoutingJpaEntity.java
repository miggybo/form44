package com.accessrequest.infrastructure.persistence.jpa;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for AccessTypeRouting entity persistence.
 * Maps to the access_type_routing table.
 */
@Entity
@Table(name = "access_type_routing", indexes = {
    @Index(name = "idx_routing_access_type_id", columnList = "access_type_id"),
    @Index(name = "idx_routing_access_type_default", columnList = "access_type_id,is_default")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessTypeRoutingJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "routing_id", columnDefinition = "UUID")
    private UUID routingId;

    @Column(name = "access_type_id", nullable = false, columnDefinition = "UUID")
    private UUID accessTypeId;

    @Column(name = "administrator_role", nullable = false)
    private String administratorRole;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
