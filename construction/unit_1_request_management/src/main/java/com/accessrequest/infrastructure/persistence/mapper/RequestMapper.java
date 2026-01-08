package com.accessrequest.infrastructure.persistence.mapper;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestId;
import com.accessrequest.domain.valueobject.RequestStatus;
import com.accessrequest.infrastructure.persistence.jpa.RequestJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Request domain aggregate and RequestJpaEntity.
 */
@Component
public class RequestMapper {

    /**
     * Convert domain Request aggregate to JPA entity.
     *
     * @param request the domain request aggregate
     * @return the JPA entity
     */
    public RequestJpaEntity toDomain(Request request) {
        if (request == null) {
            return null;
        }

        return RequestJpaEntity.builder()
            .requestId(request.getRequestId().getValue())
            .requestorId(request.getRequestorId())
            .accessTypeId(request.getAccessTypeId())
            .systemName(request.getSystemName())
            .justification(request.getJustification().getText())
            .status(mapStatus(request.getStatus()))
            .createdAt(request.getCreatedAt())
            .submittedAt(request.getSubmittedAt())
            .approvedAt(request.getApprovedAt())
            .implementedAt(request.getImplementedAt())
            .createdBy(request.getCreatedBy())
            .updatedBy(request.getUpdatedBy())
            .updatedAt(request.getUpdatedAt())
            .build();
    }

    /**
     * Convert JPA entity to domain Request aggregate.
     *
     * @param entity the JPA entity
     * @return the domain request aggregate
     */
    public Request toDomain(RequestJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Request.builder()
            .requestId(new RequestId(entity.getRequestId()))
            .requestorId(entity.getRequestorId())
            .accessTypeId(entity.getAccessTypeId())
            .systemName(entity.getSystemName())
            .justification(entity.getJustification())
            .status(mapStatus(entity.getStatus()))
            .createdAt(entity.getCreatedAt())
            .submittedAt(entity.getSubmittedAt())
            .approvedAt(entity.getApprovedAt())
            .implementedAt(entity.getImplementedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Map domain RequestStatus to JPA RequestStatusEnum.
     *
     * @param status the domain status
     * @return the JPA status enum
     */
    private RequestJpaEntity.RequestStatusEnum mapStatus(RequestStatus status) {
        if (status == null) {
            return null;
        }

        return switch (status.getValue()) {
            case "DRAFT" -> RequestJpaEntity.RequestStatusEnum.DRAFT;
            case "PENDING_INITIAL_APPROVAL" -> RequestJpaEntity.RequestStatusEnum.PENDING_INITIAL_APPROVAL;
            case "PENDING_REVIEW" -> RequestJpaEntity.RequestStatusEnum.PENDING_REVIEW;
            case "PENDING_FINAL_APPROVAL" -> RequestJpaEntity.RequestStatusEnum.PENDING_FINAL_APPROVAL;
            case "RETURNED_TO_REVIEWER" -> RequestJpaEntity.RequestStatusEnum.RETURNED_TO_REVIEWER;
            case "APPROVED" -> RequestJpaEntity.RequestStatusEnum.APPROVED;
            case "DECLINED" -> RequestJpaEntity.RequestStatusEnum.DECLINED;
            case "IMPLEMENTED" -> RequestJpaEntity.RequestStatusEnum.IMPLEMENTED;
            default -> throw new IllegalArgumentException("Unknown status: " + status.getValue());
        };
    }

    /**
     * Map JPA RequestStatusEnum to domain RequestStatus.
     *
     * @param statusEnum the JPA status enum
     * @return the domain status
     */
    private RequestStatus mapStatus(RequestJpaEntity.RequestStatusEnum statusEnum) {
        if (statusEnum == null) {
            return null;
        }

        return switch (statusEnum) {
            case DRAFT -> RequestStatus.DRAFT;
            case PENDING_INITIAL_APPROVAL -> RequestStatus.PENDING_INITIAL_APPROVAL;
            case PENDING_REVIEW -> RequestStatus.PENDING_REVIEW;
            case PENDING_FINAL_APPROVAL -> RequestStatus.PENDING_FINAL_APPROVAL;
            case RETURNED_TO_REVIEWER -> RequestStatus.RETURNED_TO_REVIEWER;
            case APPROVED -> RequestStatus.APPROVED;
            case DECLINED -> RequestStatus.DECLINED;
            case IMPLEMENTED -> RequestStatus.IMPLEMENTED;
        };
    }
}
