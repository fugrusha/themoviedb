package com.golovko.backend.event.listener;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.event.ComplaintStatusChangedEvent;
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
public class NotifyUserOnClosedComplaintListenerTest {

    @MockBean
    private UserNotificationService userNotificationService;

    @SpyBean
    private NotifyUserOnClosedComplaintListener notifyUserOnClosedComplaintListener;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void testOnEvent() {
        ComplaintStatusChangedEvent event = new ComplaintStatusChangedEvent();
        event.setComplaintId(UUID.randomUUID());
        event.setNewStatus(ComplaintStatus.CLOSED);

        applicationEventPublisher.publishEvent(event);

        verify(notifyUserOnClosedComplaintListener, timeout(500)).onEvent(event);
        verify(userNotificationService, timeout(500))
                .notifyOnComplaintStatusChangedToClosed(event.getComplaintId());
    }

    @Test
    public void testOnEventNotClosed() {
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (status == ComplaintStatus.CLOSED) {
                continue;
            }

            ComplaintStatusChangedEvent event = new ComplaintStatusChangedEvent();
            event.setComplaintId(UUID.randomUUID());
            event.setNewStatus(status);

            applicationEventPublisher.publishEvent(event);

            verify(notifyUserOnClosedComplaintListener, never()).onEvent(event);
            verify(userNotificationService, never()).notifyOnComplaintStatusChangedToClosed(any());
        }
    }

    @Test
    public void testOnEventAsync() throws InterruptedException {
        ComplaintStatusChangedEvent event = new ComplaintStatusChangedEvent();
        event.setComplaintId(UUID.randomUUID());
        event.setNewStatus(ComplaintStatus.CLOSED);

        List<Integer> checklist = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Mockito.doAnswer(invocationOnMock -> {
            Thread.sleep(500);
            checklist.add(2);
            latch.countDown();
            return null;
        }).when(userNotificationService).notifyOnComplaintStatusChangedToClosed(event.getComplaintId());

        applicationEventPublisher.publishEvent(event);
        checklist.add(1);

        latch.await();

        Mockito.verify(notifyUserOnClosedComplaintListener).onEvent(event);
        Mockito.verify(userNotificationService).notifyOnComplaintStatusChangedToClosed(event.getComplaintId());

        Assert.assertEquals(Arrays.asList(1, 2), checklist);
    }
}
