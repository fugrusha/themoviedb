package com.golovko.backend.service.importer;

import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.PersonReadDTO;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.PersonRepository;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class PersonImporterService {

    @Autowired
    private ExternalSystemImportService externalSystemImportService;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private PersonRepository personRepository;

    @Transactional
    public Person importPersonIfNeeded(String externalPersonId) {
        UUID importedPersonId = externalSystemImportService.getImportedEntityId(Person.class, externalPersonId);

        if (importedPersonId != null) {
            return repoHelper.getReferenceIfExist(Person.class, importedPersonId);
        }

        return importPerson(externalPersonId);
    }

    @Transactional
    private Person importPerson(String externalPersonId) {
        log.info("Importing person with external id={}", externalPersonId);

        PersonReadDTO readDTO = theMovieDbClient.getPerson(externalPersonId, null);

        Person existedPerson = personRepository.findByFullName(readDTO.getName());

        if (existedPerson != null) {
            return existedPerson;
        }

        Person newPerson = createPerson(readDTO);
        externalSystemImportService.createExternalSystemImport(newPerson, externalPersonId);

        log.info("Imported person {} with external id={}", readDTO.getName(), externalPersonId);
        return newPerson;
    }

    private Person createPerson(PersonReadDTO readDTO) {
        Person person = new Person();
        person.setFirstName(readDTO.getName().split(" ")[0]);
        person.setLastName(readDTO.getName().split(" ")[1]);

        if (readDTO.getBirthday() != null) {
            person.setBirthday(readDTO.getBirthday());
        } else {
            person.setBirthday(LocalDate.of(1900, 1, 1));
        }

        if (!StringUtils.isEmpty(readDTO.getPlaceOfBirth())) {
            person.setPlaceOfBirth(readDTO.getPlaceOfBirth());
        } else {
            person.setPlaceOfBirth("Unknown");
        }

        if (!StringUtils.isEmpty(readDTO.getBiography())) {
            person.setBio(readDTO.getBiography());
        } else {
            person.setBio(String.format("Biography for %s will be added later", readDTO.getName()));
        }

        if (readDTO.getGender().equals(1)) {
            person.setGender(Gender.FEMALE);
        } else if (readDTO.getGender().equals(2)) {
            person.setGender(Gender.MALE);
        } else {
            person.setGender(Gender.UNDEFINED);
        }

        return personRepository.save(person);
    }
}
