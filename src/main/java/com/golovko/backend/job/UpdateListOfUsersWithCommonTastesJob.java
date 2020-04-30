package com.golovko.backend.job;

import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.service.UserMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateListOfUsersWithCommonTastesJob {

    @Autowired
    private UserMatchService userMatchService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${update.list.of.users.with.common.tastes.job.cron}")
    public void updateListOfUsersWithCommonTastes() {
        log.info("Job started...");

        applicationUserRepository.getUserIds().forEach(userId -> {
            try {
                userMatchService.updateUserTopMatches(userId);
            } catch (Exception e) {
                log.error("Failed to update list of users with common tastes for user: {}", userId, e);
            }
        });

        log.info("Job finished!");
    }
}
