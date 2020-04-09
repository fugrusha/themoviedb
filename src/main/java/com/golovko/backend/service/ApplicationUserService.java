package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.RepositoryHelper;
import com.golovko.backend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationUserService {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public UserReadDTO getUser(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO createUser(UserCreateDTO createDTO) {
        ApplicationUser user = translationService.translate(createDTO, ApplicationUser.class);

        UserRole userRole = userRoleRepository.findByType(UserRoleType.USER);
        user.getUserRoles().add(userRole);
        user = applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO patchUser(UUID id, UserPatchDTO patch) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        translationService.map(patch, user);
        user = applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO updateUser(UUID id, UserPutDTO update) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        translationService.map(update, user);
        user = applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public void deleteUser(UUID id) {
        applicationUserRepository.delete(repoHelper.getEntityById(ApplicationUser.class, id));
    }

    public UserReadDTO ban(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        user.setIsBlocked(true);
        applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO pardon(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        user.setIsBlocked(false);
        applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO changeTrustLevel(UUID id, UserTrustLevelDTO dto) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        user.setTrustLevel(dto.getTrustLevel());
        applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public PageResult<UserReadDTO> getAllUsers(Pageable pageable) {
        Page<ApplicationUser> users = applicationUserRepository.getAllUsers(pageable);
        return translationService.toPageResult(users, UserReadDTO.class);
    }

    public List<UserInLeaderBoardDTO> getUsersLeaderBoard() {
        return applicationUserRepository.getUsersLeaderBoard();
    }
}
