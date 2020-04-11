package com.golovko.backend.repository;

import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.person.PersonInLeaderBoardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface PersonRepository extends CrudRepository<Person, UUID> {

    @Query("select p from Person p")
    Page<Person> findAllPeople(Pageable pageable);

    @Query("select p.id from Person p")
    Stream<UUID> getIdsOfPeople();

    @Query("select new com.golovko.backend.dto.person.PersonInLeaderBoardDTO(p.id, p.firstName, p.lastName,"
            + " p.averageRatingByRoles, (select count(mc) from MovieCast mc where mc.person.id = p.id"
            + " and mc.averageRating is not null))"
            + " from Person p"
            + " order by p.averageRatingByRoles desc")
    List<PersonInLeaderBoardDTO> getPersonLeaderBoard();
}
