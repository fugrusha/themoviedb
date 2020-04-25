package com.golovko.backend.service;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import com.golovko.backend.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@DBRider
public class MovieCastServiceDbRiderTest extends BaseTest {

    @Autowired
    private MovieCastService movieCastService;

    @Test
    @DataSet(value = "/datasets/testCalcAverageRatingOfMovieCast.xml")
    @ExpectedDataSet(value = "/datasets/testCalcAverageRatingOfMovieCast_result.xml")
    public void testCalcAverageRatingOfMovieCast() {
        UUID movieCastId = UUID.fromString("52050b6b-eefc-4f5f-9c22-58b29223be09");
        movieCastService.updateAverageRatingOfMovieCast(movieCastId);
    }

}
