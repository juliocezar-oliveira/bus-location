package com.projeto.bus_location.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // desativa CSRF para Postman e testes
                .csrf(csrf -> csrf.disable())

                // configura autorização
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/veiculos/**").authenticated() // exige login
                        .anyRequest().permitAll() // libera outros endpoints
                )

                // autenticação básica moderna
                .httpBasic(httpBasic -> {});

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.builder()
                .username("admin")
                .password("1234")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // NoOp para teste; em produção use BCryptPasswordEncoder
        return NoOpPasswordEncoder.getInstance();
    }
}
