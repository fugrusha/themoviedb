package com.golovko.backend.service.importer;

import com.golovko.backend.BaseTest;
import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.PersonReadDTO;
import com.golovko.backend.domain.ExternalSystemImport;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.ImportedEntityType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.ExternalSystemImportRepository;
import com.golovko.backend.repository.PersonRepository;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.util.UUID;

import static com.golovko.backend.domain.Gender.FEMALE;
import static com.golovko.backend.domain.ImportedEntityType.PERSON;
import static org.mockito.ArgumentMatchers.any;

public class PersonImporterServiceTest extends BaseTest {

    @MockBean
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private ExternalSystemImportRepository esiRepository;

    @Autowired
    private PersonRepository personRepository;

    @SpyBean
    private PersonImporterService personImporterService;

    @Test
    public void testImportAlreadyImportedPerson() {
        String externalPersonId = "id1000";

        Person p1 = testObjectFactory.createPerson();
        createESI(p1.getId(), externalPersonId, ImportedEntityType.PERSON);

        Person person = personImporterService.importPersonIfNeeded(externalPersonId);

        Mockito.verify(theMovieDbClient, Mockito.never()).getPerson(any(), any());
        Assert.assertEquals(p1.getId(), person.getId());
    }

    @Test
    public void testImportAlreadyExistedPerson() {
        String externalPersonId = "id1000";

        Person p1 = testObjectFactory.createPerson(); // create person
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setName(p1.getFirstName() + " " + p1.getLastName());

        Mockito.when(theMovieDbClient.getPerson(externalPersonId, null)).thenReturn(personDTO);

        Person person = personImporterService.importPersonIfNeeded(externalPersonId);

        Assert.assertEquals(p1.getId(), person.getId());
    }

    @Test
    public void testImportPersonWithNullFields() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setBirthday(null);
        personDTO.setBiography(null);
        personDTO.setPlaceOfBirth(null);

        String expectedBio = String.format("Biography for %s will be added later", personDTO.getName());
        LocalDate expectedBirthday = LocalDate.of(1900, 1, 1);
        String expectedPlaceOfBirth = "Unknown";

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        personImporterService.importPersonIfNeeded(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(expectedBio, savedPerson.getBio());
        Assert.assertEquals(expectedBirthday, savedPerson.getBirthday());
        Assert.assertEquals(expectedPlaceOfBirth, savedPerson.getPlaceOfBirth());
    }

    @Test
    public void testImportPersonWithMaleGender() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setGender(2); // male

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        personImporterService.importPersonIfNeeded(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(Gender.MALE, savedPerson.getGender());
    }

    @Test
    public void testImportPersonWithFemaleGender() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setGender(1); // female

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        personImporterService.importPersonIfNeeded(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(FEMALE, savedPerson.getGender());
    }

    @Test
    public void testImportPersonWithUndefinedGender() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setGender(0); // undefined

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        personImporterService.importPersonIfNeeded(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(Gender.UNDEFINED, savedPerson.getGender());
    }

    private ExternalSystemImport createESI(
            UUID entityId,
            String idInExternalSystem,
            ImportedEntityType entityType
    ) {
        ExternalSystemImport esi = new ExternalSystemImport();
        esi.setEntityId(entityId);
        esi.setIdInExternalSystem(idInExternalSystem);
        esi.setEntityType(entityType);
        return esiRepository.save(esi);
    }

    private PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = generateObject(PersonReadDTO.class);
        dto.setName("Alisa Winter");
        dto.setBirthday("1988-05-12");
        dto.setGender(2);
        return dto;
    }
}
