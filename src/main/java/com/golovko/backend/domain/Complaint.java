package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Complaint extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 128)
    private String complaintTitle;

    @NotNull
    @Size(min = 1, max = 1000)
    private String complaintText;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ComplaintType complaintType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ComplaintStatus complaintStatus;

    @NotNull
    @ManyToOne
    private ApplicationUser author;

    @ManyToOne
    private ApplicationUser moderator;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TargetObjectType targetObjectType;

    @NotNull
    private UUID targetObjectId;
}
