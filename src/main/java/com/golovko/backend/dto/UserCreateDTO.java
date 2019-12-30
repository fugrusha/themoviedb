package com.golovko.backend.dto;

import java.util.Objects;

public class UserCreateDTO {

    private String username;
    private String password;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateDTO createDTO = (UserCreateDTO) o;
        return username.equals(createDTO.username) &&
                password.equals(createDTO.password) &&
                email.equals(createDTO.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, email);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
