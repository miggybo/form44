package com.accessrequest.domain.repository;

import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.valueobject.AccessTypeId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AccessTypeRepository Interface.
 * 
 * Defines persistence operations for AccessType aggregates.
 */
public interface AccessTypeRepository {

    /**
     * Save an access type.
     */
    void save(AccessType accessType);

    /**
     * Update an access type.
     */
    void update(AccessType accessType);

    /**
     * Find access type by ID.
     */
    Optional<AccessType> findById(AccessTypeId accessTypeId);

    /**
     * Find access type by UUID.
     */
    Optional<AccessType> findById(UUID accessTypeId);

    /**
     * Find access type by name.
     */
    Optional<AccessType> findByName(String name);

    /**
     * Find all access types.
     */
    List<AccessType> findAll();

    /**
     * Find access types by routing role.
     */
    List<AccessType> findByRoutingRole(String role);

    /**
     * Delete an access type.
     */
    void delete(AccessTypeId accessTypeId);

}
