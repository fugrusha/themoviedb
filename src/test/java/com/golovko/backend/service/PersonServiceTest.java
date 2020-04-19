package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.person.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ArticleRepository;
import com.golovko.backend.repository.PersonRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PersonServiceTest extends BaseTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void testGetPerson() {
        Person person = testObjectFactory.createPerson();

        PersonReadDTO readDTO = personService.getPerson(person.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(person);
    }

    @Test
    public void testGetPersonExtended() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        article.setPeople(List.of(person));
        articleRepository.save(article);

        PersonReadExtendedDTO actualResult = personService.getPersonExtended(person.getId());

        Assertions.assertThat(actualResult).isEqualToIgnoringGivenFields(person,
                "articles", "movieCasts", "movieCrews");

        Assertions.assertThat(actualResult.getArticles()).extracting("id")
                .containsExactlyInAnyOrder(article.getId());
        Assertions.assertThat(actualResult.getMovieCasts()).extracting("id")
                .containsExactlyInAnyOrder(movieCast.getId());
        Assertions.assertThat(actualResult.getMovieCrews()).extracting("id")
                .containsExactlyInAnyOrder(movieCrew.getId());
    }

    @Test
    public void testGetAllPeople() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Person p3 = testObjectFactory.createPerson();

        PageResult<PersonReadDTO> result = personService.getPeople(Pageable.unpaged());

        Assertions.assertThat(result.getData()).extracting("id")
                .containsExactlyInAnyOrder(p1.getId(), p3.getId(), p2.getId());
    }

    @Test
    public void testGetAllPeopleWithPagingAndSorting() {
        Person p1 = testObjectFactory.createPerson("Akulova");
        Person p2 = testObjectFactory.createPerson("Hefner");
        testObjectFactory.createPerson("Moldovan");

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "lastName"));

        Assertions.assertThat(personService.getPeople(pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(p1.getId(), p2.getId()));
    }

    @Test
    public void testGetTopRatedPerson() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Person p3 = testObjectFactory.createPerson();

        List<PersonTopRatedDTO> actualResult = personService.getTopRatedPeople();

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(p1.getId(), p2.getId(), p3.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetPersonWrongId() {
        personService.getPerson(UUID.randomUUID());
    }

    @Test
    public void testCreatePerson() {
        PersonCreateDTO createDTO = new PersonCreateDTO();
        createDTO.setFirstName("Max");
        createDTO.setLastName("Popov");
        createDTO.setBio("some text");
        createDTO.setGender(Gender.MALE);

        PersonReadDTO readDTO = personService.createPerson(createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Person person = personRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(person);
    }

    @Test
    public void testPatchPerson() {
        Person person = testObjectFactory.createPerson();

        PersonPatchDTO patchDTO = new PersonPatchDTO();
        patchDTO.setFirstName("Lolita");
        patchDTO.setLastName("Bulgakova");
        patchDTO.setBio("some bio");
        patchDTO.setGender(Gender.FEMALE);
        patchDTO.setBirthday(LocalDate.of(1990, 10, 10));
        patchDTO.setImageUrl("url");
        patchDTO.setPlaceOfBirth("city");

        PersonReadDTO readDTO = personService.patchPerson(person.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        person = personRepository.findById(person.getId()).get();
        Assertions.assertThat(person).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCasts", "articles");
    }

    @Test
    public void testPatchPersonEmptyPatch() {
        Person person = testObjectFactory.createPerson();
        PersonPatchDTO patchDTO = new PersonPatchDTO();

        PersonReadDTO readDTO = personService.patchPerson(person.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("averageRatingByRoles",
                "averageRatingByMovies");

        Person personAfterUpdate = personRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(personAfterUpdate).hasNoNullFieldsOrPropertiesExcept("averageRatingByRoles",
                "averageRatingByMovies");
        Assertions.assertThat(person).isEqualToIgnoringGivenFields(personAfterUpdate,
                "movieCrews", "movieCasts", "articles");
    }

    @Test
    public void testUpdatePerson() {
        Person person = testObjectFactory.createPerson();

        PersonPutDTO updateDTO = new PersonPutDTO();
        updateDTO.setFirstName("Lolita");
        updateDTO.setLastName("Bulgakova");
        updateDTO.setBio("some text");
        updateDTO.setGender(Gender.FEMALE);

        PersonReadDTO readDTO = personService.updatePerson(person.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        person = personRepository.findById(person.getId()).get();
        Assertions.assertThat(person).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCasts", "articles");
    }

    @Test
    public void testDeletePerson() {
        Person person = testObjectFactory.createPerson();
        personService.deletePerson(person.getId());

        Assert.assertFalse(personRepository.existsById(person.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeletePersonNotFound() {
        personService.deletePerson(UUID.randomUUID());
    }

    @Test
    public void testUpdateAverageRatingOfPerson() {
        Person p1 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        testObjectFactory.createMovieCast(p1, m1, 5.0);
        testObjectFactory.createMovieCast(p1, m2, 3.0);

        personService.updateAverageRatingOfPersonRoles(p1.getId());

        p1 = personRepository.findById(p1.getId()).get();

        Assert.assertEquals(4.0, p1.getAverageRatingByRoles(), Double.MIN_NORMAL);
    }

    @Test
    public void testUpdateAverageRatingOfPersonMovies() {
        Movie m1 = testObjectFactory.createMovie(5.0);
        Movie m2 = testObjectFactory.createMovie(4.0);
        Person p1 = testObjectFactory.createPerson();

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p1, m2);

        personService.updateAverageRatingOfPersonMovies(p1.getId());

        p1 = personRepository.findById(p1.getId()).get();

        Assert.assertEquals(4.5, p1.getAverageRatingByMovies(), Double.MIN_NORMAL);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSavePersonNotNullValidation() {
        Person person = new Person();
        personRepository.save(person);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSavePersonMaxSizeValidation() {
        Person person = new Person();
        person.setFirstName("very long text".repeat(100));
        person.setLastName("very long text".repeat(100));
        person.setBio("very long text".repeat(1000));
        person.setGender(Gender.MALE);
        personRepository.save(person);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSavePersonMinSizeValidation() {
        Person person = new Person();
        person.setFirstName("");
        person.setLastName("");
        person.setBio("");
        person.setGender(Gender.MALE);
        personRepository.save(person);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSavePersonMinRatingValidation() {
        Person person = new Person();
        person.setFirstName("Name");
        person.setLastName("Surname");
        person.setBio("bio");
        person.setGender(Gender.MALE);
        person.setAverageRatingByMovies(-0.01);
        person.setAverageRatingByRoles(-0.01);
        personRepository.save(person);
        ;
    }

    @Test(expected = TransactionSystemException.class)
    public void testSavePersonMaxRatingValidation() {
        Person person = new Person();
        person.setFirstName("Name");
        person.setLastName("Surname");
        person.setBio("bio");
        person.setGender(Gender.MALE);
        person.setAverageRatingByMovies(10.01);
        person.setAverageRatingByRoles(10.01);
        personRepository.save(person);
        ;
    }
}
