package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserPutDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApplicationUserService {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public UserReadDTO getUser(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        return translationService.toRead(user);
    }

    public UserReadDTO createUser(UserCreateDTO createDTO) {
        ApplicationUser user = translationService.toEntity(createDTO);

        user.getUserRole().add(UserRole.USER);

        user = applicationUserRepository.save(user);
        return translationService.toRead(user);
    }

    public UserReadDTO patchUser(UUID id, UserPatchDTO patch) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        translationService.patchEntity(patch, user);

        user = applicationUserRepository.save(user);
        return translationService.toRead(user);
    }

    public UserReadDTO updateUser(UUID id, UserPutDTO update) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        translationService.updateEntity(update, user);

        user = applicationUserRepository.save(user);
        return translationService.toRead(user);
    }

    public void deleteUser(UUID id) {
        applicationUserRepository.delete(repoHelper.getEntityById(ApplicationUser.class, id));
    }

    public void ban(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        user.setIsBlocked(true);
        applicationUserRepository.save(user);
    }

    public void pardon(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        user.setIsBlocked(false);
        applicationUserRepository.save(user);
    }
}
