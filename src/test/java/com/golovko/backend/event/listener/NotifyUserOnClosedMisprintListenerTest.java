package com.golovko.backend.event.listener;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.event.MisprintStatusChangedEvent;
import com.golovko.backend.service.UserNotificationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class NotifyUserOnClosedMisprintListenerTest {

    @MockBean
    private UserNotificationService userNotificationService;

    @SpyBean
    private NotifyUserOnClosedMisprintListener notifyUserOnClosedMisprintListener;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void testOnEvent() {
        MisprintStatusChangedEvent event = new MisprintStatusChangedEvent();
        event.setMisprintId(UUID.randomUUID());
        event.setNewStatus(ComplaintStatus.CLOSED);

        applicationEventPublisher.publishEvent(event);

        verify(notifyUserOnClosedMisprintListener).onEvent(event);
        verify(userNotificationService).notifyOnMisprintStatusChangedToClosed(event.getMisprintId());
    }

    @Test
    public void testOnEventNotClosed() {
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (status == ComplaintStatus.CLOSED) {
                continue;
            }

            MisprintStatusChangedEvent event = new MisprintStatusChangedEvent();
            event.setMisprintId(UUID.randomUUID());
            event.setNewStatus(status);

            applicationEventPublisher.publishEvent(event);

            verify(notifyUserOnClosedMisprintListener, never()).onEvent(event);
            verify(userNotificationService, never()).notifyOnMisprintStatusChangedToClosed(any());
        }
    }

    @Test
    public void testOnEventAsync() throws InterruptedException {
        MisprintStatusChangedEvent event = new MisprintStatusChangedEvent();
        event.setMisprintId(UUID.randomUUID());
        event.setNewStatus(ComplaintStatus.CLOSED);

        List<Integer> checklist = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Mockito.doAnswer(invocationOnMock -> {
            Thread.sleep(500);
            checklist.add(2);
            latch.countDown();
            return null;
        }).when(userNotificationService).notifyOnMisprintStatusChangedToClosed(event.getMisprintId());

        applicationEventPublisher.publishEvent(event);
        checklist.add(1);

        latch.await();

        Mockito.verify(notifyUserOnClosedMisprintListener, timeout(500)).onEvent(event);
        Mockito.verify(userNotificationService, timeout(500))
                .notifyOnMisprintStatusChangedToClosed(event.getMisprintId());

        Assert.assertEquals(Arrays.asList(1, 2), checklist);
    }
}
