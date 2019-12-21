package com.golovko.backend.service;

import com.golovko.backend.domain.User;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserReadDTO getUser(UUID id) {
        User user = userRepository.findById(id).get();
        return toRead(user);
    }

    private UserReadDTO toRead(User user) {
        UserReadDTO dto = new UserReadDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setActive(user.isActive());
        return dto;
    }


}
