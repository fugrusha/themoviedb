package com.golovko.backend.event;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class MisprintStatusChangedEvent {

    private UUID misprintId;

    private ComplaintStatus newStatus;
}
