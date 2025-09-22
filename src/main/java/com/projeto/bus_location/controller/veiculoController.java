package com.projeto.bus_location.controller;

import com.projeto.bus_location.model.posicaoVeiculo;
import com.projeto.bus_location.repository.posicaoVeiculoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
public class veiculoController {

    private final posicaoVeiculoRepository repository;

    public veiculoController(posicaoVeiculoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{line}")
    public List<posicaoVeiculo> getPosicaoVeiculoByLine(@PathVariable String line) {
        return repository.findByLine(line);
    }

    @PostMapping
    public posicaoVeiculo addVehiclePosition(@RequestBody posicaoVeiculo position) {
        return repository.save(position);
    }
}
