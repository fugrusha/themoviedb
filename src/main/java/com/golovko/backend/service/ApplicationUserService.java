package com.golovko.backend.service;

import com.golovko.backend.config.SecurityConfig;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.exception.UserAlreadyExistsException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.RepositoryHelper;
import com.golovko.backend.repository.UserRoleRepository;
import com.golovko.backend.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private SecurityConfig securityConfig;

    @Autowired
    private RepositoryHelper repoHelper;

    public UserReadDTO getUser(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        return translationService.translate(user, UserReadDTO.class);
    }

    @Transactional(readOnly = true)
    public UserReadExtendedDTO getExtendedUser(UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);
        return translationService.translate(user, UserReadExtendedDTO.class);
    }

    @Transactional
    public UserReadDTO createUser(UserCreateDTO createDTO) {
        if (applicationUserRepository.findByEmail(createDTO.getEmail()) != null) {
            throw new UserAlreadyExistsException(createDTO.getEmail());
        }

        ApplicationUser user = translationService.translate(createDTO, ApplicationUser.class);

        user.setEncodedPassword(securityConfig.passwordEncoder().encode(createDTO.getPassword()));
        user.getUserRoles().add(userRoleRepository.findByType(UserRoleType.USER));
        user = applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO patchUser(UUID id, UserPatchDTO patch) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        translationService.map(patch, user);

        if (!Utils.empty(patch.getPassword())) {
            user.setEncodedPassword(securityConfig.passwordEncoder().encode(patch.getPassword()));
        }

        user = applicationUserRepository.save(user);

        return translationService.translate(user, UserReadDTO.class);
    }

    public UserReadDTO updateUser(UUID id, UserPutDTO update) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, id);

        translationService.map(update, user);
        if (!Utils.empty(update.getPassword())) {
            user.setEncodedPassword(securityConfig.passwordEncoder().encode(update.getPassword()));
        }
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
