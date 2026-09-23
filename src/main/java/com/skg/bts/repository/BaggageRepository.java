package com.skg.bts.repository;

import com.skg.bts.domain.Baggage;
import com.skg.bts.domain.enums.BaggageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BaggageRepository extends JpaRepository<Baggage, Long> {

    Optional<Baggage> findByTagNumber(String tagNumber);

    List<Baggage> findByCurrentStatus(BaggageStatus status);

    // Used by the stuck-bag sweep: bags not yet mishandled/delivered, untouched since `threshold`.
    List<Baggage> findByCurrentStatusNotInAndUpdatedAtBefore(
            List<BaggageStatus> excludedStatuses, Instant threshold);
}
