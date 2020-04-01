package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class UserRole extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    private UserRoleType type;

    @ManyToMany(mappedBy = "userRoles")
    private List<ApplicationUser> users = new ArrayList<>();
}
