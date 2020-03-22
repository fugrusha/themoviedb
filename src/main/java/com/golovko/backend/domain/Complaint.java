package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Complaint extends AbstractEntity {

    private String complaintTitle;

    private String complaintText;

    @Enumerated(EnumType.STRING)
    private ComplaintType complaintType;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus complaintStatus;

    @ManyToOne
    private ApplicationUser author;

    @ManyToOne
    private ApplicationUser moderator;

    @Enumerated(EnumType.STRING)
    private TargetObjectType targetObjectType;

    @Column(nullable = false)
    private UUID targetObjectId;
}
