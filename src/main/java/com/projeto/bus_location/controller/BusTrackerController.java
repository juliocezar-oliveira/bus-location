package com.projeto.bus_location.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "*")
public class BusTrackerController {

    private final WebClient webClient;
    private static final String SPTRANS_API_URL = "http://api.olhovivo.sptrans.com.br/v2.1";
    private static final String SPTRANS_AUTH_URL = "http://api.olhovivo.sptrans.com.br/v2.1/Login/Autenticar";
    private static final String SPTRANS_API_AUTH_TOKEN = "a3c700a6e73f8c101b8e77806859f4e66237021219b87507275efd78e1d831b6";
    private static String accessToken = null;

    public BusTrackerController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(SPTRANS_API_URL).build();
        try {
            authenticate();
        } catch (Exception e) {
            System.err.println("Erro ao autenticar na inicialização: " + e.getMessage());
        }
    }

    public static class BusPosition {
        private double latitude;
        private double longitude;

        public BusPosition(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    private void authenticate() throws Exception {
        URL url = new URL(SPTRANS_AUTH_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie", "apiCredentials=" + SPTRANS_API_AUTH_TOKEN);
        connection.setDoOutput(true);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            accessToken = "Bearer " + SPTRANS_API_AUTH_TOKEN;
            System.out.println("Autenticação bem-sucedida!");
        } else {
            throw new Exception("Falha na autenticação: " + responseCode);
        }
    }

    @GetMapping("/api/track")
    public Mono<ResponseEntity<List<BusPosition>>> trackBusLine(@RequestParam(name = "line") String lineCode) {
        System.out.println("Requisição para a linha: " + lineCode);


        if (accessToken == null) {
            try {
                authenticate();
            } catch (Exception e) {
                System.err.println("Não foi possível reautenticar. " + e.getMessage());
                return Mono.just(new ResponseEntity<>(Collections.<BusPosition>emptyList(), HttpStatus.SERVICE_UNAVAILABLE));
            }
        }


        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Posicao/Linha")
                        .queryParam("codigoLinha", lineCode.toUpperCase())
                        .build())
                .header("Authorization", accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonResponse -> {
                    try {
                        JSONObject json = new JSONObject(jsonResponse);
                        JSONArray vehicles = json.getJSONArray("vs");
                        List<BusPosition> busPositions = new ArrayList<>();
                        for (int i = 0; i < vehicles.length(); i++) {
                            JSONObject vehicle = vehicles.getJSONObject(i);
                            double lat = vehicle.getDouble("py");
                            double lon = vehicle.getDouble("px");
                            busPositions.add(new BusPosition(lat, lon));
                        }
                        return new ResponseEntity<>(busPositions, HttpStatus.OK);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar JSON: " + e.getMessage());
                        return new ResponseEntity<>(Collections.<BusPosition>emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Erro na chamada da API: " + e.getMessage());
                    return Mono.just(new ResponseEntity<>(Collections.<BusPosition>emptyList(), HttpStatus.SERVICE_UNAVAILABLE));
                });
    }
}
