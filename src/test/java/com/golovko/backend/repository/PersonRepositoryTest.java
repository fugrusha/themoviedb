package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.person.PersonTopRatedDTO;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PersonRepositoryTest extends BaseTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Test
    public void testCreatedAtIsSet() {
        Person person = testObjectFactory.createPerson();

        Instant createdAtBeforeReload = person.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        person = personRepository.findById(person.getId()).get();

        Instant createdAtAfterReload = person.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        Person person = testObjectFactory.createPerson();

        Instant modifiedAtBeforeReload = person.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        person = personRepository.findById(person.getId()).get();
        person.setFirstName("Another Person Name");
        person = personRepository.save(person);
        Instant modifiedAtAfterReload = person.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetPeople() {
        Person p1 = testObjectFactory.createPerson("Akulova");
        Person p2 = testObjectFactory.createPerson("Moldovan");
        Person p3 = testObjectFactory.createPerson("Hefner");

        Page<Person> result = personRepository.findAllPeople(Pageable.unpaged());

        Assertions.assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(p1.getId(), p2.getId(), p3.getId());
    }

    @Test
    public void testGetIdsOfPeople() {
        Set<UUID> expectedResult = new HashSet<>();
        expectedResult.add(testObjectFactory.createPerson().getId());
        expectedResult.add(testObjectFactory.createPerson().getId());
        expectedResult.add(testObjectFactory.createPerson().getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = personRepository.getIdsOfPeople().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    @Test
    public void testFindByFullName() {
        Person p1 = testObjectFactory.createPerson();
        p1.setFirstName("Alex");
        p1.setLastName("Cooper");

        Person p2 = testObjectFactory.createPerson();
        p2.setFirstName("Bob");       // wrong Name
        p2.setLastName("Cooper");

        personRepository.saveAll(List.of(p1, p2));

        Person person = personRepository.findByFullName("Alex Cooper");

        Assert.assertEquals(p1.getId(), person.getId());
    }

    @Test
    public void testGetTopRatedPerson() {
        Set<UUID> personIds = new HashSet<>();
        for (int i = 0; i < 100; ++i) {
            Movie m = testObjectFactory.createMovie();
            Person p = createPerson();
            personIds.add(p.getId());

            createMovieCast(p, m, true);
            createMovieCast(p, m, true);
            createMovieCast(p, m, false);
        }

        List<PersonTopRatedDTO> actualResult = personRepository.getTopRatedPeople();

        Assertions.assertThat(actualResult).isSortedAccordingTo(
                Comparator.comparing(PersonTopRatedDTO::getAverageRatingByRoles).reversed());

        Assert.assertEquals(personIds, actualResult.stream()
                .map(PersonTopRatedDTO::getId)
                .collect(Collectors.toSet()));

        for (PersonTopRatedDTO p : actualResult) {
            Assert.assertNotNull(p.getFirstName());
            Assert.assertNotNull(p.getLastName());
            Assert.assertNotNull(p.getAverageRatingByRoles());
            Assert.assertEquals( 2, p.getRolesCount().longValue());
        }
    }

    @Test
    public void testCalcPersonAverageRatingByMovieId() {
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        Person p1 = testObjectFactory.createPerson(5.0);
        Person p2 = testObjectFactory.createPerson(3.0);
        Person p3 = testObjectFactory.createPerson(4.0);

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p2, m1);
        testObjectFactory.createMovieCast(p3, m2);

        Double averageRating = personRepository.calcMovieCastAverageRatingByMovieId(m1.getId());

        Assert.assertEquals(4.0, averageRating, Double.MIN_NORMAL);
    }

    private Person createPerson() {
        Person person = new Person();
        person.setFirstName("name");
        person.setLastName("surname");
        person.setGender(Gender.MALE);
        person.setBio("text");
        person.setAverageRatingByRoles(ThreadLocalRandom.current().nextDouble(1, 10));
        return personRepository.save(person);
    }

    private MovieCast createMovieCast(Person person, Movie movie, boolean withRating) {
        MovieCast movieCast = new MovieCast();
        movieCast.setCharacter("character");
        movieCast.setDescription("character");
        movieCast.setMovieCrewType(MovieCrewType.CAST);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);

        if (withRating) {
            movieCast.setAverageRating(ThreadLocalRandom.current().nextDouble(1, 10));
        } else {
            movieCast.setAverageRating(null);
        }

        return movieCastRepository.save(movieCast);
    }
}
