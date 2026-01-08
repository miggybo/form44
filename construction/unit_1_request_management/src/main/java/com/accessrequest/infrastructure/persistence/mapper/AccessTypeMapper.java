package com.accessrequest.infrastructure.persistence.mapper;

import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.valueobject.AccessTypeId;
import com.accessrequest.domain.valueobject.AccessTypeName;
import com.accessrequest.domain.valueobject.AccessTypeDescription;
import com.accessrequest.infrastructure.persistence.jpa.AccessTypeJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AccessType domain aggregate and AccessTypeJpaEntity.
 */
@Component
public class AccessTypeMapper {

    /**
     * Convert domain AccessType aggregate to JPA entity.
     *
     * @param accessType the domain access type aggregate
     * @return the JPA entity
     */
    public AccessTypeJpaEntity toDomain(AccessType accessType) {
        if (accessType == null) {
            return null;
        }

        return AccessTypeJpaEntity.builder()
            .accessTypeId(accessType.getAccessTypeId().getValue())
            .name(accessType.getName().getValue())
            .description(accessType.getDescription() != null ? accessType.getDescription().getValue() : null)
            .createdAt(accessType.getCreatedAt())
            .updatedAt(accessType.getUpdatedAt())
            .createdBy(accessType.getCreatedBy())
            .updatedBy(accessType.getUpdatedBy())
            .build();
    }

    /**
     * Convert JPA entity to domain AccessType aggregate.
     *
     * @param entity the JPA entity
     * @return the domain access type aggregate
     */
    public AccessType toDomain(AccessTypeJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccessType.builder()
            .accessTypeId(new AccessTypeId(entity.getAccessTypeId()))
            .name(new AccessTypeName(entity.getName()))
            .description(entity.getDescription() != null ? new AccessTypeDescription(entity.getDescription()) : null)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
