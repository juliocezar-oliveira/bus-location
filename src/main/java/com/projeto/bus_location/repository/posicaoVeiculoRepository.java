package com.projeto.bus_location.repository;

import com.projeto.bus_location.model.posicaoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface posicaoVeiculoRepository extends JpaRepository<posicaoVeiculo, Long> {
    List<posicaoVeiculo> findByLine(String line);
}
