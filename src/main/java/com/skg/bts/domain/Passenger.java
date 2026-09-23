package com.skg.bts.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "passengers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false, length = 10)
    private String pnr;

    @Column(name = "seat_no", length = 5)
    private String seatNo;
}
