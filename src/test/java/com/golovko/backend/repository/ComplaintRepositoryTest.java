package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from complaint", "delete from application_user"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ComplaintRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Test
    public void getComplaintsByUser() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, ComplaintType.CHILD_ABUSE);
        Complaint c2 = testObjectFactory.createComplaint(user1, ComplaintType.CHILD_ABUSE);
        testObjectFactory.createComplaint(user2, ComplaintType.CHILD_ABUSE);
        testObjectFactory.createComplaint(user2, ComplaintType.MISPRINT);

        List<Complaint> result = complaintRepository.findByAuthorIdAndComplaintType(user1.getId(), ComplaintType.CHILD_ABUSE);
        Assertions.assertThat(result).extracting(Complaint::getId).isEqualTo(Arrays.asList(c1.getId(), c2.getId()));
    }
}
