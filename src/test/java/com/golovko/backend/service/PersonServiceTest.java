package com.golovko.backend.service;

import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.PersonRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from person", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void getPersonTest() {
        Person person = testObjectFactory.createPerson();

        PersonReadDTO readDTO = personService.getPerson(person.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(person);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getPersonWrongIdTest() {
        personService.getPerson(UUID.randomUUID());
    }

    @Test
    public void createPersonTest() {
        PersonCreateDTO createDTO = new PersonCreateDTO();
        createDTO.setFirstName("Max");
        createDTO.setLastName("Popov");
        createDTO.setGender(Gender.MALE);

        PersonReadDTO readDTO = personService.createPerson(createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Person person = personRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(person);
    }

    @Test
    public void patchPersonTest() {
        Person person = testObjectFactory.createPerson();

        PersonPatchDTO patchDTO = new PersonPatchDTO();
        patchDTO.setFirstName("Lolita");
        patchDTO.setLastName("Bulgakova");
        patchDTO.setGender(Gender.FEMALE);

        PersonReadDTO readDTO = personService.patchPerson(person.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        person = personRepository.findById(person.getId()).get();
        Assertions.assertThat(person).isEqualToIgnoringGivenFields(readDTO, "movieParticipations");
    }

    @Test
    public void patchPersonEmptyPatchTest() {
        Person person = testObjectFactory.createPerson();
        PersonPatchDTO patchDTO = new PersonPatchDTO();

        PersonReadDTO readDTO = personService.patchPerson(person.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getFirstName());
        Assert.assertNotNull(readDTO.getLastName());
        Assert.assertNotNull(readDTO.getGender());

        Person personAfterUpdate = personRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(personAfterUpdate.getFirstName());
        Assert.assertNotNull(personAfterUpdate.getLastName());
        Assert.assertNotNull(personAfterUpdate.getGender());

        Assertions.assertThat(person).isEqualToIgnoringGivenFields(personAfterUpdate, "movieParticipations");
    }

    @Test
    public void deletePersonTest() {
        Person person = testObjectFactory.createPerson();
        personService.deletePerson(person.getId());

        Assert.assertFalse(personRepository.existsById(person.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deletePersonNotFoundTest() {
        personService.deletePerson(UUID.randomUUID());
    }
}
