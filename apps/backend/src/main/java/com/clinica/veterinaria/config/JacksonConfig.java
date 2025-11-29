package com.clinica.veterinaria.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;

import java.util.TimeZone;

/**
 * Configuración de Jackson para el manejo correcto de fechas y horas.
 * 
 * <p>Esta configuración asegura que las fechas se manejen correctamente,
 * especialmente cuando se reciben del frontend sin zona horaria explícita.
 * Las fechas sin zona horaria se interpretan como hora local del servidor.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(@NonNull Jackson2ObjectMapperBuilder builder) {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        return builder
            .modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .timeZone(defaultTimeZone)
            .build();
    }
}

