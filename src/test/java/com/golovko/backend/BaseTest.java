package com.golovko.backend;

import com.golovko.backend.util.TestObjectFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from like",
        "delete from misprint",
        "delete from rating",
        "delete from genre_movie",
        "delete from genre",
        "delete from comment",
        "delete from complaint",
        "delete from article",
        "delete from user_role",
        "delete from application_user",
        "delete from movie_cast",
        "delete from movie_crew",
        "delete from person",
        "delete from movie"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public abstract class BaseTest {

    @Autowired
    protected TestObjectFactory testObjectFactory;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    protected void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
