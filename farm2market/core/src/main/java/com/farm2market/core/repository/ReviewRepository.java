package com.farm2market.core.repository;

import com.farm2market.core.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends Repository<Review, String> {

    Optional<Review> findByOrderId(String orderId);

    List<Review> findByFarmerId(String farmerId);

    List<Review> findByFarmerId(String farmerId, int limit, int offset);

    List<Review> findByCustomerId(String customerId);

    List<Review> findByCustomerId(String customerId, int limit, int offset);

    boolean existsByOrderId(String orderId);

    double getAverageRatingByFarmerId(String farmerId);

    long countByFarmerId(String farmerId);

    RatingSummary getRatingSummary(String farmerId);

    class RatingSummary {
        private final double average;
        private final long totalCount;
        private final long[] ratingCounts; // index 0 = 1 star, index 4 = 5 stars

        public RatingSummary(double average, long totalCount, long[] ratingCounts) {
            this.average = average;
            this.totalCount = totalCount;
            this.ratingCounts = ratingCounts;
        }

        public double getAverage() { return average; }
        public long getTotalCount() { return totalCount; }
        public long[] getRatingCounts() { return ratingCounts; }
        public long getCountForRating(int rating) {
            return rating >= 1 && rating <= 5 ? ratingCounts[rating - 1] : 0;
        }
    }
}
