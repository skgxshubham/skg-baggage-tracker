package com.skg.bts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skg.bts.domain.OutboxEvent;
import com.skg.bts.dto.BaggageEvent;
import com.skg.bts.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Writes the "to-do" row in the SAME transaction as the real database save.
// This is the whole point of the outbox pattern: the event record and the
// actual change are saved together, so neither can happen without the other.
@Service
@RequiredArgsConstructor
public class OutboxService {

    private static final String TOPIC = "baggage-events";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void enqueue(BaggageEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(OutboxEvent.builder()
                    .topic(TOPIC)
                    .eventKey(event.tagNumber())
                    .payload(json)
                    .build());
        } catch (Exception e) {
            // If this fails, something is seriously wrong (e.g. the event can't be
            // serialized at all) — unlike the cache, we DO want this to be visible,
            // since losing an outbox write defeats the entire point of this pattern.
            throw new RuntimeException("Failed to enqueue outbox event for " + event.tagNumber(), e);
        }
    }
}