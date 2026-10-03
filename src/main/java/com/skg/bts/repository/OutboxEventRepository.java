package com.skg.bts.repository;

import com.skg.bts.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findBySentFalseOrderByCreatedAtAsc();
}