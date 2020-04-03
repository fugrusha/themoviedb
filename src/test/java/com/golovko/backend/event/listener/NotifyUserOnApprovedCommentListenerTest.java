package com.golovko.backend.event.listener;

import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.event.CommentStatusChangedEvent;
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
public class NotifyUserOnApprovedCommentListenerTest {

    @MockBean
    private UserNotificationService userNotificationService;

    @SpyBean
    private NotifyUserOnApprovedCommentListener notifyUserOnApprovedCommentListener;

    @SpyBean
    private NotifyUserOnBlockedCommentListener notifyUserOnBlockedCommentListener;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void testOnEventCommentApproved() {
        CommentStatusChangedEvent event = new CommentStatusChangedEvent();
        event.setCommentId(UUID.randomUUID());
        event.setNewStatus(CommentStatus.APPROVED);

        applicationEventPublisher.publishEvent(event);

        verify(notifyUserOnApprovedCommentListener, timeout(500)).onEvent(event);
        verify(userNotificationService, timeout(500))
                .notifyOnCommentStatusChangedToApproved(event.getCommentId());
    }

    @Test
    public void testOnEventCommentNotApproved() {
        for (CommentStatus status : CommentStatus.values()) {
            if (status == CommentStatus.APPROVED) {
                continue;
            }

            CommentStatusChangedEvent event = new CommentStatusChangedEvent();
            event.setCommentId(UUID.randomUUID());
            event.setNewStatus(status);

            applicationEventPublisher.publishEvent(event);

            verify(notifyUserOnApprovedCommentListener, never()).onEvent(event);
            verify(userNotificationService, never()).notifyOnCommentStatusChangedToApproved(any());
        }
    }

    @Test
    public void testOnEventCommentApprovedAsync() throws InterruptedException {
        CommentStatusChangedEvent event = new CommentStatusChangedEvent();
        event.setCommentId(UUID.randomUUID());
        event.setNewStatus(CommentStatus.APPROVED);

        List<Integer> checklist = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Mockito.doAnswer(invocationOnMock -> {
            Thread.sleep(500);
            checklist.add(2);
            latch.countDown();
            return null;
        }).when(userNotificationService).notifyOnCommentStatusChangedToApproved(event.getCommentId());

        applicationEventPublisher.publishEvent(event);
        checklist.add(1);

        latch.await();

        Mockito.verify(notifyUserOnApprovedCommentListener).onEvent(event);
        Mockito.verify(userNotificationService).notifyOnCommentStatusChangedToApproved(event.getCommentId());

        Assert.assertEquals(Arrays.asList(1, 2), checklist);
    }

    @Test
    public void testOnEventCommentBlocked() {
        CommentStatusChangedEvent event = new CommentStatusChangedEvent();
        event.setCommentId(UUID.randomUUID());
        event.setNewStatus(CommentStatus.BLOCKED);

        applicationEventPublisher.publishEvent(event);

        verify(notifyUserOnBlockedCommentListener, timeout(500)).onEvent(event);
        verify(userNotificationService, timeout(500))
                .notifyOnCommentStatusChangedToBlocked(event.getCommentId());
    }

    @Test
    public void testOnEventCommentNotBlocked() {
        for (CommentStatus status : CommentStatus.values()) {
            if (status == CommentStatus.BLOCKED) {
                continue;
            }

            CommentStatusChangedEvent event = new CommentStatusChangedEvent();
            event.setCommentId(UUID.randomUUID());
            event.setNewStatus(status);

            applicationEventPublisher.publishEvent(event);

            verify(notifyUserOnBlockedCommentListener, never()).onEvent(event);
            verify(userNotificationService, never()).notifyOnCommentStatusChangedToBlocked(any());
        }
    }

    @Test
    public void testOnEventCommentBlockedAsync() throws InterruptedException {
        CommentStatusChangedEvent event = new CommentStatusChangedEvent();
        event.setCommentId(UUID.randomUUID());
        event.setNewStatus(CommentStatus.BLOCKED);

        List<Integer> checklist = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Mockito.doAnswer(invocationOnMock -> {
            Thread.sleep(500);
            checklist.add(2);
            latch.countDown();
            return null;
        }).when(userNotificationService).notifyOnCommentStatusChangedToBlocked(event.getCommentId());

        applicationEventPublisher.publishEvent(event);
        checklist.add(1);

        latch.await();

        Mockito.verify(notifyUserOnBlockedCommentListener).onEvent(event);
        Mockito.verify(userNotificationService).notifyOnCommentStatusChangedToBlocked(event.getCommentId());

        Assert.assertEquals(Arrays.asList(1, 2), checklist);
    }
}
