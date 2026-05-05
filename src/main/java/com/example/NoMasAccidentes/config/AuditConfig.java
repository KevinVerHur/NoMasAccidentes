package com.example.NoMasAccidentes.config;


import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/*
 * Activa la auditoría JPA (campos @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
 * heredados desde BaseEntity).
 */

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
    /**
     * Devuelve el usuario autenticado actual para llenar los campos creado_por / actualizado_por.
     * Si no hay nadie autenticado (ej. tareas programadas) usa "system".
     */

    @Bean
    public AuditorAware<String> auditorProvider() {
        return ()->{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth==null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }



}
