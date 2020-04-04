package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class ComplaintModerateDTO {

    @NotNull
    private UUID moderatorId;

    @NotNull
    private ComplaintStatus complaintStatus;

    @Size(min = 1, max = 500)
    private String newCommentMessage;

    private Boolean deleteComment;

    private Boolean blockCommentAuthor;

    private Boolean decreaseComplaintAuthorTrustLevelByOne;
}
