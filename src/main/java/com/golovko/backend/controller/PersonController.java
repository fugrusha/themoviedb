package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.person.*;
import com.golovko.backend.service.PersonService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/people")
public class PersonController {

    @Autowired
    private PersonService personService;

    @ApiOperation(value = "Get single person")
    @GetMapping("/{id}")
    public PersonReadDTO getPerson(@PathVariable UUID id) {
        return personService.getPerson(id);
    }

    @ApiOperation(value = "Get single person with details")
    @GetMapping("/{id}/extended")
    public PersonReadExtendedDTO getPersonExtended(@PathVariable UUID id) {
        return personService.getPersonExtended(id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all people")
    @GetMapping
    public PageResult<PersonReadDTO> getAllPeople(@ApiIgnore Pageable pageable) {
        return personService.getPeople(pageable);
    }

    @ApiOperation(value = "Get top rated people",
            notes = "People sorted by average rating for roles")
    @GetMapping("/top-rated")
    public List<PersonTopRatedDTO> getTopRatedPeople() {
        return personService.getTopRatedPeople();
    }

    @ApiOperation(value = "Create person", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @PostMapping
    public PersonReadDTO createPerson(@RequestBody @Valid PersonCreateDTO createDTO) {
        return personService.createPerson(createDTO);
    }

    @ApiOperation(value = "Update person",
            notes = "Needs CONTENT_MANAGER authority. Empty fields will not be updated")
    @ContentManager
    @PatchMapping("/{id}")
    public PersonReadDTO patchPerson(@RequestBody @Valid PersonPatchDTO patchDTO, @PathVariable UUID id) {
        return personService.patchPerson(id, patchDTO);
    }

    @ApiOperation(value = "Update person",
            notes = "Needs CONTENT_MANAGER authority. All fields will not updated")
    @ContentManager
    @PutMapping("/{id}")
    public PersonReadDTO updatePerson(@RequestBody @Valid PersonPutDTO updateDTO, @PathVariable UUID id) {
        return personService.updatePerson(id, updateDTO);
    }

    @ApiOperation(value = "Delete person", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
    }
}
