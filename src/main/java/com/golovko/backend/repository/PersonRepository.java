package com.golovko.backend.repository;

import com.golovko.backend.domain.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface PersonRepository extends CrudRepository<Person, UUID> {

    @Query("select p from Person p order by p.lastName asc")
    List<Person> findAllPeople();

    @Query("select p.id from Person p")
    Stream<UUID> getIdsOfPersons();
}
