package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Complaint {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String complaintTitle;

    private String complaintText;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private ComplaintType complaintType;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus complaintStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "author_id")
    private ApplicationUser author;

    @Column(nullable = false)
    private UUID parentId;
}
