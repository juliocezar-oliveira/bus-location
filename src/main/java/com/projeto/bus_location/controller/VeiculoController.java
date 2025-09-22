package com.projeto.bus_location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projeto.bus_location.model.PosicaoVeiculo;
import com.projeto.bus_location.repository.PosicaoVeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final PosicaoVeiculoRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public VeiculoController(PosicaoVeiculoRepository repository, RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PosicaoVeiculo addPosicaoVeiculo(@RequestBody PosicaoVeiculo position) {

        PosicaoVeiculo saved = repository.save(position);

        String line = saved.getLine();
        String cacheKey = "linha::posicoes::" + line;
        redisTemplate.delete(cacheKey);
        String redisChannel = "veiculo::linha::" + line + "::atualizacao";
        redisTemplate.convertAndSend(redisChannel, saved);
        String websocketTopic = "/topic/linha/" + line;
        messagingTemplate.convertAndSend(websocketTopic, saved);

        return saved;
    }

    @GetMapping("/linha/{line}")
    public List<PosicaoVeiculo> getPosicaoVeiculoByLine(@PathVariable String line) {
        String cacheKey = "linha::posicoes::" + line;

        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

        if (cachedValue != null) {
            try {
                List<PosicaoVeiculo> cachedList = mapper.convertValue(cachedValue, mapper.getTypeFactory().constructCollectionType(List.class, PosicaoVeiculo.class));
                return cachedList;
            } catch (IllegalArgumentException e) {
            }
        }

        List<PosicaoVeiculo> posicoes = repository.findByLine(line);

        if (!posicoes.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, posicoes, Duration.ofMinutes(5));
        }

        return posicoes;
    }



    private void saveToRedis(PosicaoVeiculo posicao) {
        String busKey = "veiculo::posicao::" + posicao.getId();

        redisTemplate.opsForHash().put(busKey, "data", posicao);
        redisTemplate.expire(busKey, 6, TimeUnit.HOURS);

        String historyKey = "veiculo::historico::" + posicao.getId();

        redisTemplate.opsForZSet().add(historyKey, posicao, System.currentTimeMillis());
        redisTemplate.expire(historyKey, 24, TimeUnit.HOURS);
    }
}