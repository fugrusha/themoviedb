package com.golovko.backend.repository;

import com.golovko.backend.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;


public interface UserRepository extends CrudRepository<User, UUID> {
}
