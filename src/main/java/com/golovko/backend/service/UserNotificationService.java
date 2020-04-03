package com.golovko.backend.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserNotificationService {

    public void notifyOnMisprintStatusChangedToClosed(UUID misprintId) {

    }

    public void notifyOnComplaintStatusChangedToClosed(UUID complaintId) {

    }

    public void notifyOnCommentStatusChangedToApproved(UUID commentId) {

    }

    public void notifyOnCommentStatusChangedToBlocked(UUID commentId) {

    }
}
