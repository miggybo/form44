package com.accessrequest.infrastructure.persistence.repository;

import com.accessrequest.infrastructure.persistence.jpa.AccessTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AccessType aggregate persistence.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface AccessTypeJpaRepository extends JpaRepository<AccessTypeJpaEntity, UUID> {

    /**
     * Find access type by name.
     *
     * @param name the access type name
     * @return optional containing the access type if found
     */
    Optional<AccessTypeJpaEntity> findByName(String name);

    /**
     * Find all access types with routing rules for a specific administrator role.
     *
     * @param administratorRole the administrator role
     * @return list of access types
     */
    @Query("SELECT DISTINCT a FROM AccessTypeJpaEntity a " +
           "JOIN a.routingRules r " +
           "WHERE r.administratorRole = :administratorRole")
    List<AccessTypeJpaEntity> findByRoutingRole(@Param("administratorRole") String administratorRole);

    /**
     * Find access type with default routing for a specific administrator role.
     *
     * @param administratorRole the administrator role
     * @return optional containing the access type if found
     */
    @Query("SELECT a FROM AccessTypeJpaEntity a " +
           "JOIN a.routingRules r " +
           "WHERE r.administratorRole = :administratorRole AND r.isDefault = true")
    Optional<AccessTypeJpaEntity> findDefaultByRoutingRole(@Param("administratorRole") String administratorRole);

    /**
     * Check if access type name exists.
     *
     * @param name the access type name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Check if access type name exists excluding a specific ID.
     *
     * @param name the access type name
     * @param accessTypeId the access type ID to exclude
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AccessTypeJpaEntity a " +
           "WHERE a.name = :name AND a.accessTypeId != :accessTypeId")
    boolean existsByNameExcludingId(@Param("name") String name, @Param("accessTypeId") UUID accessTypeId);
}
