package com.skg.bts.repository;

import com.skg.bts.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByBaggageIdOrderBySentAtDesc(Long baggageId);
}
