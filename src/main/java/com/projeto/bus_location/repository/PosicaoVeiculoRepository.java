package com.projeto.bus_location.repository;

import com.projeto.bus_location.model.PosicaoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Certifique-se de que esta é a importação correta do @Param

import java.util.List;

public interface PosicaoVeiculoRepository extends JpaRepository<PosicaoVeiculo, Long> {

    List<PosicaoVeiculo> findByLine(String line);

    @Query("SELECT p FROM PosicaoVeiculo p WHERE p.line = :line AND p.id IN " +
            "(SELECT MAX(pv.id) FROM PosicaoVeiculo pv WHERE pv.line = :line GROUP BY pv.vehicleId)")
    List<PosicaoVeiculo> findLatestPositionsByLine(@Param("line") String line);
}