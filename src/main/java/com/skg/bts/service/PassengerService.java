package com.skg.bts.service;

import com.skg.bts.domain.Passenger;
import com.skg.bts.exception.ResourceNotFoundException;
import com.skg.bts.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public Passenger getEntityById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found: " + id));
    }
}
