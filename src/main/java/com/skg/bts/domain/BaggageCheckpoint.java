package com.skg.bts.domain;

import com.skg.bts.domain.enums.BaggageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "baggage_checkpoints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BaggageCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baggage_id", nullable = false)
    private Baggage baggage;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkpoint_type", nullable = false, length = 20)
    private BaggageStatus checkpointType;

    @Column(length = 3)
    private String location;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 255)
    private String notes;

    @PrePersist
    void onCreate() {
        if (timestamp == null) timestamp = Instant.now();
    }
}
