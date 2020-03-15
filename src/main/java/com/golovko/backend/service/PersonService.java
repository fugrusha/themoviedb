package com.golovko.backend.service;

import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.repository.PersonRepository;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public List<PersonReadDTO> getAllPersons() {
        List<Person> people = personRepository.findAllPeople();
        return people.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public PersonReadDTO getPerson(UUID id) {
        Person person = repoHelper.getEntityById(Person.class, id);
        return translationService.toRead(person);
    }

    public PersonReadDTO createPerson(PersonCreateDTO createDTO) {
        Person person = translationService.toEntity(createDTO);

        person = personRepository.save(person);

        return translationService.toRead(person);
    }

    public PersonReadDTO patchPerson(UUID id, PersonPatchDTO patchDTO) {
        Person person = repoHelper.getEntityById(Person.class, id);

        translationService.patchEntity(patchDTO, person);

        person = personRepository.save(person);

        return translationService.toRead(person);
    }

    public PersonReadDTO updatePerson(UUID id, PersonPutDTO updateDTO) {
        Person person = repoHelper.getEntityById(Person.class, id);

        translationService.updateEntity(updateDTO, person);

        person = personRepository.save(person);

        return translationService.toRead(person);
    }

    public void deletePerson(UUID id) {
        personRepository.delete(repoHelper.getEntityById(Person.class, id));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAverageRatingOfPerson(UUID personId) {
        Double averageRating = movieCastRepository.calcAverageRatingOfPerson(personId);
        Person person = repoHelper.getEntityById(Person.class, personId);

        log.info("Setting new average rating of person: {}. Old value {}, new value {}", personId,
                person.getAverageRatingByRoles(), averageRating);

        person.setAverageRatingByRoles(averageRating);
        personRepository.save(person);
    }
}
