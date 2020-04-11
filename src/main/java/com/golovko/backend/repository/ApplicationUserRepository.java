package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.user.UserInLeaderBoardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationUserRepository extends CrudRepository<ApplicationUser, UUID> {

    @Query("select u from ApplicationUser u")
    Page<ApplicationUser> getAllUsers(Pageable pageable);

    @Query("select new com.golovko.backend.dto.user.UserInLeaderBoardDTO(u.id, u.username, u.trustLevel,"
            + " (select count(c) from Comment c where c.author.id = u.id"
            + " and c.targetObjectType = com.golovko.backend.domain.TargetObjectType.MOVIE),"
            + " (select count(r) from Rating r where r.author.id = u.id"
            + " and r.ratedObjectType = com.golovko.backend.domain.TargetObjectType.MOVIE))"
            + " from ApplicationUser u"
            + " order by u.trustLevel desc")
    List<UserInLeaderBoardDTO> getUsersLeaderBoard();

}