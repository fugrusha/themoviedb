package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.UserCreateDTO;
import com.golovko.backend.dto.UserPatchDTO;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApplicationUserService {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private TranslationService translationService;

    public UserReadDTO getUser(UUID id) {
        ApplicationUser applicationUser = getUserRequired(id);
        return translationService.toRead(applicationUser);
    }

    public UserReadDTO createUser(UserCreateDTO createDTO) {
        ApplicationUser applicationUser = translationService.toEntity(createDTO);

        applicationUser = applicationUserRepository.save(applicationUser);
        return translationService.toRead(applicationUser);
    }

    public UserReadDTO patchUser(UUID id, UserPatchDTO patch) {

        ApplicationUser applicationUser = getUserRequired(id);

        translationService.patchEntity(patch, applicationUser);

        applicationUser = applicationUserRepository.save(applicationUser);
        return translationService.toRead(applicationUser);
    }

    public void deleteUser(UUID id) {
        applicationUserRepository.delete(getUserRequired(id));
    }

    private ApplicationUser getUserRequired(UUID id) {
        return applicationUserRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(ApplicationUser.class, id)
        );
    }
}
