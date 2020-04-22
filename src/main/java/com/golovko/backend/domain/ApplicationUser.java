package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class ApplicationUser extends AbstractEntity {

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    private Boolean isBlocked = false;

    @NotNull
    private Double trustLevel = 1.0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "user_role_id"))
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();
}
