package com.skg.bts.service;

import com.skg.bts.domain.OutboxEvent;
import com.skg.bts.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

// Polls the outbox table and actually publishes to Kafka. This is the one
// piece that can fail without losing anything — the row just stays "unsent"
// and gets picked up again on the next run.
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "PT5S")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findBySentFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload()).get();
                event.setSent(true);
                event.setSentAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.warn("Failed to publish outbox event {} — will retry next cycle: {}", event.getId(), e.getMessage());
                // Deliberately no rethrow: we want the loop to continue to the next
                // event rather than one bad message blocking everything behind it.
            }
        }
    }
}