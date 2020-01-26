package com.golovko.backend.dto.user;

import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserReadExtendedDTO {

    private UUID id;

    private String username;

    private String email;

    private List<ComplaintReadDTO> complaints;
}
