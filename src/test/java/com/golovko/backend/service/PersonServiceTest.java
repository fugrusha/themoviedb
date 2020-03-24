package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.PersonRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class PersonServiceTest extends BaseTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void testGetPerson() {
        Person person = testObjectFactory.createPerson();

        PersonReadDTO readDTO = personService.getPerson(person.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(person);
    }

    @Test
    public void testGetAllPersons() {
        Person p1 = createPerson("Akulova");
        Person p2 = createPerson("Moldovan");
        Person p3 = createPerson("Hefner");
        Person p4 = createPerson("Buzova");

        List<PersonReadDTO> result = personService.getAllPersons();

        Assertions.assertThat(result).extracting("id")
                .containsExactly(p1.getId(), p4.getId(), p3.getId(), p2.getId());
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

        PersonReadDTO readDTO = personService.patchPerson(person.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        person = personRepository.findById(person.getId()).get();
        Assertions.assertThat(person).isEqualToIgnoringGivenFields(readDTO,
                        "movieCrews", "movieCast");
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
                        "movieCrews", "movieCast");
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
                "movieCrews", "movieCast");
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

    private Person createPerson(String lastName) {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName(lastName);
        person.setBio("some text");
        person.setAverageRatingByRoles(5.0);
        person.setGender(Gender.FEMALE);
        return personRepository.save(person);
    }
}
