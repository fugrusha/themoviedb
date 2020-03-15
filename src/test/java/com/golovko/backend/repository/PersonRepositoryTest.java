package com.golovko.backend.repository;

import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Person;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(statements = "delete from person", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

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
        Person p4 = createPerson("Buzova");

        List<Person> result = personRepository.findAllPeople();

        Assertions.assertThat(result).extracting("id")
                .containsExactly(p1.getId(), p4.getId(), p3.getId(), p2.getId());
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
