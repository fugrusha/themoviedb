package com.golovko.backend.service;

import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    TranslationService translationService;

    public PersonReadDTO getPerson(UUID id) {
        Person person = getRequiredPerson(id);
        return translationService.toRead(person);
    }

    public PersonReadDTO createPerson(PersonCreateDTO createDTO) {
        Person person = translationService.toEntity(createDTO);

        person = personRepository.save(person);

        return translationService.toRead(person);
    }

    public PersonReadDTO patchPerson(UUID id, PersonPatchDTO patchDTO) {
        Person person = getRequiredPerson(id);

        translationService.patchEntity(patchDTO, person);

        person = personRepository.save(person);

        return translationService.toRead(person);
    }

    public void deletePerson(UUID id) {
        personRepository.delete(getRequiredPerson(id));
    }

    private Person getRequiredPerson(UUID id) {
        return personRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Person.class, id));
    }
}
