package com.golovko.backend.controller;

import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    @Autowired
    private PersonService personService;

    @GetMapping("/{id}")
    public PersonReadDTO getPerson(@PathVariable UUID id) {
        return personService.getPerson(id);
    }

    @PostMapping
    public PersonReadDTO createPerson(@RequestBody PersonCreateDTO createDTO) {
        return personService.createPerson(createDTO);
    }

    @PatchMapping("/{id}")
    public PersonReadDTO patchPerson(@RequestBody PersonPatchDTO patchDTO, @PathVariable UUID id) {
        return personService.patchPerson(id, patchDTO);
    }
//
//    @PutMapping
//    public PersonReadDTO updatePerson(@RequestBody PersonPatchDTO patchDTO, @PathVariable UUID id) {
//        return personService.updatePerson(id, patchDTO);
//    }
//
    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable UUID id) {
         personService.deletePerson(id);
    }
}
