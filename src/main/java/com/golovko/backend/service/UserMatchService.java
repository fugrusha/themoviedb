package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Rating;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.RatingRepository;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserMatchService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private RepositoryHelper reposHelper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserTopMatches(UUID userId) {
        ApplicationUser user = reposHelper.getEntityById(ApplicationUser.class, userId);
        user.getTopMatches().clear();
        log.info("All {}'s topMatches were removed", user.getUsername());

        List<UUID> topTenMatchingUserIds = getIdsOfNewMatches(userId);

        List<ApplicationUser> newMatches = new ArrayList<>();

        for (UUID id : topTenMatchingUserIds) {
            ApplicationUser match = reposHelper.getReferenceIfExist(ApplicationUser.class, id);
            newMatches.add(match);
        }

        user.getTopMatches().addAll(newMatches);
        applicationUserRepository.save(user);

        log.info("{}'s top 10 matches were updated", user.getUsername());
    }

    private List<UUID> getIdsOfNewMatches(UUID userId) {
        List<Rating> userRatings = ratingRepository.findByAuthorId(userId);

        Map<UUID, Double> correlationMap = new HashMap<>();

        applicationUserRepository.getUserIds().forEach(id -> {
            if (!id.equals(userId)) {
                List<Rating> otherUserRatings = ratingRepository.findByAuthorId(id);
                Double corrCoefficient = calcCorrelationForSimilarRatedObjects(userRatings, otherUserRatings);
                correlationMap.put(id, corrCoefficient);

                log.info("Calculated correlation coefficient = {}", corrCoefficient);
            }
        });

        log.info("Found {} users with similar tastes", correlationMap.size());

        return correlationMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Double calcCorrelationForSimilarRatedObjects(List<Rating> userRatings, List<Rating> otherUserRatings) {
        List<Double> currentUserRatings = new ArrayList<>();
        List<Double> anotherUserRatings = new ArrayList<>();

        for (Rating userRating : userRatings) {
            for (Rating otherUserRating : otherUserRatings) {
                if (userRating.getRatedObjectId().equals(otherUserRating.getRatedObjectId())) {
                    currentUserRatings.add(userRating.getRating().doubleValue());
                    anotherUserRatings.add(otherUserRating.getRating().doubleValue());
                }
            }
        }

        log.info("Found {} common rated objects", currentUserRatings.size());

        if (currentUserRatings.size() > 1) {
            double[] xArray = currentUserRatings.stream().mapToDouble(Double::doubleValue).toArray();
            double[] yArray = anotherUserRatings.stream().mapToDouble(Double::doubleValue).toArray();

            return calcPearsonCorrelation(xArray, yArray);
        }

        return 0.0;
    }

    private double calcPearsonCorrelation(double[] xArray, double[] yArray) {
        return new PearsonsCorrelation().correlation(xArray, yArray);
    }
}
