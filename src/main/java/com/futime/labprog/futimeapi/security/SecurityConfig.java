package com.futime.labprog.futimeapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // Habilita CORS no Spring Security
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF pois é uma API REST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/competicoes/**", "/clubes/**",
                                "/jogadores/**", "/estadios/**", "/partidas/**")
                        .permitAll()
                        .anyRequest().authenticated() // Todo o resto exige autenticação
                )
                .httpBasic(withDefaults()); // Habilita autenticação Basic (usuário/senha)

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
