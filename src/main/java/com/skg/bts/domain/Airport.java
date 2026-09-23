package com.skg.bts.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "airports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;
}
