package com.golovko.backend.repository;

import com.golovko.backend.domain.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface PersonRepository extends CrudRepository<Person, UUID> {

    @Query("select p from Person p")
    Page<Person> findAllPeople(Pageable pageable);

    @Query("select p.id from Person p")
    Stream<UUID> getIdsOfPersons();
}
