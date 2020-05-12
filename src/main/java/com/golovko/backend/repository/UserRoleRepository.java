package com.golovko.backend.repository;

import com.golovko.backend.domain.UserRole;
import com.golovko.backend.domain.UserRoleType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, UUID> {

    @Query("select role.id from UserRole role where role.type = :userRoleType")
    UUID findUserRoleIdByType(UserRoleType userRoleType);

    UserRole findByType(UserRoleType userRoleType);

    @Query("select r from UserRole r")
    List<UserRole> findAllRoles();
}
