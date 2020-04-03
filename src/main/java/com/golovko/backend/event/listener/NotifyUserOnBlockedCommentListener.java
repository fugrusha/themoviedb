package com.golovko.backend.event.listener;

import com.golovko.backend.event.CommentStatusChangedEvent;
import com.golovko.backend.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotifyUserOnBlockedCommentListener {

    @Autowired
    private UserNotificationService userNotificationService;

    @Async
    @EventListener(condition = "#event.newStatus == T(com.golovko.backend.domain.CommentStatus).BLOCKED")
    public void onEvent(CommentStatusChangedEvent event) {
        log.info("Handling {}", event);

        userNotificationService.notifyOnCommentStatusChangedToBlocked(event.getCommentId());
    }
}
