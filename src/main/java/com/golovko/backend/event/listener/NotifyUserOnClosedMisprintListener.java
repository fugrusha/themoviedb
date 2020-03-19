package com.golovko.backend.event.listener;

import com.golovko.backend.event.MisprintStatusChangedEvent;
import com.golovko.backend.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotifyUserOnClosedMisprintListener {

    @Autowired
    private UserNotificationService userNotificationService;

    @Async
    @EventListener(condition = "#event.newStatus == T(com.golovko.backend.domain.ComplaintStatus).CLOSED")
    public void onEvent(MisprintStatusChangedEvent event) {
        log.info("Handling {}", event);

        userNotificationService.notifyOnMisprintStatusChangedToClosed(event.getMisprintId());
    }
}
