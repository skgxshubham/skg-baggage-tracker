package com.skg.bts.repository;

import com.skg.bts.domain.BaggageCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaggageCheckpointRepository extends JpaRepository<BaggageCheckpoint, Long> {
    List<BaggageCheckpoint> findByBaggageIdOrderByTimestampAsc(Long baggageId);
}
