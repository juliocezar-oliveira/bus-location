package com.projeto.bus_location.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class posicaoVeiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private String line;
    private double latitude;
    private double longitude;

    private LocalDateTime timestamp;

    // Getters e Setters
}
