package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.person.*;
import com.golovko.backend.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/people")
public class PersonController {

    @Autowired
    private PersonService personService;

    @GetMapping("/{id}")
    public PersonReadDTO getPerson(@PathVariable UUID id) {
        return personService.getPerson(id);
    }

    @GetMapping("/{id}/extended")
    public PersonReadExtendedDTO getPersonExtended(@PathVariable UUID id) {
        return personService.getPersonExtended(id);
    }

    @GetMapping
    public PageResult<PersonReadDTO> getAllPeople(Pageable pageable) {
        return personService.getPeople(pageable);
    }

    @GetMapping("/leader-board")
    public List<PersonInLeaderBoardDTO> getPersonLeaderBoard() {
        return personService.getPersonLeaderBoard();
    }

    @PostMapping
    public PersonReadDTO createPerson(@RequestBody @Valid PersonCreateDTO createDTO) {
        return personService.createPerson(createDTO);
    }

    @PatchMapping("/{id}")
    public PersonReadDTO patchPerson(@RequestBody @Valid PersonPatchDTO patchDTO, @PathVariable UUID id) {
        return personService.patchPerson(id, patchDTO);
    }

    @PutMapping("/{id}")
    public PersonReadDTO updatePerson(@RequestBody @Valid PersonPutDTO updateDTO, @PathVariable UUID id) {
        return personService.updatePerson(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
    }
}
