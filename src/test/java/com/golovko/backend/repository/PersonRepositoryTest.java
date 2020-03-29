package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Person;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PersonRepositoryTest extends BaseTest {

    @Autowired
    private PersonRepository personRepository;

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
    public void testGetPersonOrderByLastNameAsc() {
        Person p1 = createPerson("Akulova");
        Person p2 = createPerson("Moldovan");
        Person p3 = createPerson("Hefner");

        Page<Person> result = personRepository.findAllPeople(Pageable.unpaged());

        Assertions.assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(p1.getId(), p2.getId(), p3.getId());
    }

    @Test
    public void testGetIdsOfPersons() {
        Set<UUID> expectedResult = new HashSet<>();
        expectedResult.add(testObjectFactory.createPerson().getId());
        expectedResult.add(testObjectFactory.createPerson().getId());
        expectedResult.add(testObjectFactory.createPerson().getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = personRepository.getIdsOfPersons().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    private Person createPerson(String lastName) {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName(lastName);
        person.setBio("some text");
        person.setGender(Gender.FEMALE);
        return personRepository.save(person);
    }
}
