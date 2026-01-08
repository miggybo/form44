package com.accessrequest.infrastructure.persistence.jpa;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for AccessType aggregate persistence.
 * Maps the AccessType domain aggregate to the access_types table.
 */
@Entity
@Table(name = "access_types", indexes = {
    @Index(name = "idx_access_type_name", columnList = "name", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessTypeJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "access_type_id", columnDefinition = "UUID")
    private UUID accessTypeId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "UUID")
    private UUID updatedBy;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "access_type_id")
    @Builder.Default
    private List<AccessTypeRoutingJpaEntity> routingRules = new ArrayList<>();
}
