package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationUserRepository extends CrudRepository<ApplicationUser, UUID> {
}