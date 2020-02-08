package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Complaint {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String complaintTitle;

    @Column(nullable = false)
    private String complaintText;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComplaintType complaintType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComplaintStatus complaintStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "author_id")
    private ApplicationUser author;
}
