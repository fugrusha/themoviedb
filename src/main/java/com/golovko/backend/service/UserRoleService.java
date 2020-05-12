package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.RepositoryHelper;
import com.golovko.backend.repository.UserRoleRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserRoleService {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Transactional
    public List<UserRoleReadDTO> getUserRoles(UUID userId) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, userId);

        if (CollectionUtils.isEmpty(user.getUserRoles())) {
            throw new EntityNotFoundException("User " + userId + " has not any role.");
        }

        return translationService.translateList(user.getUserRoles(), UserRoleReadDTO.class);
    }

    @Transactional
    public List<UserRoleReadDTO> addUserRole(UUID userId, UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, userId);
        UserRole userRole = repoHelper.getEntityById(UserRole.class, id);

        if (user.getUserRoles().stream().anyMatch(role -> role.getId().equals(id))) {
            throw new LinkDuplicatedException(String.format("User %s already has role %s", userId, id));
        }

        user.getUserRoles().add(userRole);
        user = applicationUserRepository.save(user);

        return translationService.translateList(user.getUserRoles(), UserRoleReadDTO.class);
    }

    @Transactional
    public List<UserRoleReadDTO> removeRoleFromUser(UUID userId, UUID id) {
        ApplicationUser user = repoHelper.getEntityById(ApplicationUser.class, userId);

        boolean removed = user.getUserRoles().removeIf(role -> role.getId().equals(id));

        if (!removed) {
            throw new EntityNotFoundException("User " + userId + " has no role " + id);
        }

        user = applicationUserRepository.save(user);

        return translationService.translateList(user.getUserRoles(), UserRoleReadDTO.class);
    }

    public List<UserRoleReadDTO> getAllUserRoles() {
        List<UserRole> allRoles = userRoleRepository.findAllRoles();

        return translationService.translateList(allRoles, UserRoleReadDTO.class);

    }
}
