package com.golovko.backend;

import com.golovko.backend.util.TestObjectFactory;
import org.bitbucket.brunneng.br.RandomObjectGenerator;
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
        "delete from external_system_import",
        "delete from like_entity",
        "delete from misprint",
        "delete from rating",
        "delete from genre_movie",
        "delete from genre",
        "delete from comment",
        "delete from complaint",
        "delete from article",
        "delete from user_user_role",
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

    private final RandomObjectGenerator generator = new RandomObjectGenerator();

    protected <T> T generateObject(Class<T> objectClass) {
        return generator.generateRandomObject(objectClass);
    }

    protected void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
