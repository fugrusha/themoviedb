package com.golovko.backend.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('MODERATOR') or (hasAuthority('USER') and @currentUserValidator.isCurrentUser(#userId))")
public @interface ModeratorOrCurrentUser {
}
