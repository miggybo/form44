package com.accessrequest.infrastructure.persistence.repository;

import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.repository.AccessTypeRepository;
import com.accessrequest.infrastructure.persistence.jpa.AccessTypeJpaEntity;
import com.accessrequest.infrastructure.persistence.mapper.AccessTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AccessTypeRepository using Spring Data JPA.
 * Bridges the domain layer with the infrastructure layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessTypeRepositoryImpl implements AccessTypeRepository {

    private final AccessTypeJpaRepository jpaRepository;
    private final AccessTypeMapper mapper;

    @Override
    public void save(AccessType accessType) {
        log.debug("Saving access type with ID: {}", accessType.getAccessTypeId());
        AccessTypeJpaEntity entity = mapper.toDomain(accessType);
        jpaRepository.save(entity);
        log.debug("Access type saved successfully");
    }

    @Override
    public void update(AccessType accessType) {
        log.debug("Updating access type with ID: {}", accessType.getAccessTypeId());
        AccessTypeJpaEntity entity = mapper.toDomain(accessType);
        jpaRepository.save(entity);
        log.debug("Access type updated successfully");
    }

    @Override
    public void delete(UUID accessTypeId) {
        log.debug("Deleting access type with ID: {}", accessTypeId);
        jpaRepository.deleteById(accessTypeId);
        log.debug("Access type deleted successfully");
    }

    @Override
    public Optional<AccessType> findById(UUID accessTypeId) {
        log.debug("Finding access type by ID: {}", accessTypeId);
        return jpaRepository.findById(accessTypeId)
            .map(mapper::toDomain)
            .map(entity -> {
                log.debug("Access type found: {}", accessTypeId);
                return entity;
            });
    }

    @Override
    public Optional<AccessType> findByName(String name) {
        log.debug("Finding access type by name: {}", name);
        return jpaRepository.findByName(name)
            .map(mapper::toDomain)
            .map(entity -> {
                log.debug("Access type found by name: {}", name);
                return entity;
            });
    }

    @Override
    public List<AccessType> findAll() {
        log.debug("Finding all access types");
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AccessType> findByRoutingRole(String role) {
        log.debug("Finding access types by routing role: {}", role);
        return jpaRepository.findByRoutingRole(role).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<AccessType> findDefaultByRoutingRole(String role) {
        log.debug("Finding default access type by routing role: {}", role);
        return jpaRepository.findDefaultByRoutingRole(role)
            .map(mapper::toDomain)
            .map(entity -> {
                log.debug("Default access type found for role: {}", role);
                return entity;
            });
    }

    @Override
    public boolean existsByName(String name) {
        log.debug("Checking if access type exists by name: {}", name);
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameExcludingId(String name, UUID accessTypeId) {
        log.debug("Checking if access type exists by name excluding ID: {} - {}", name, accessTypeId);
        return jpaRepository.existsByNameExcludingId(name, accessTypeId);
    }
}
