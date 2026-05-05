package com.example.NoMasAccidentes.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración manual de Flyway.
 * Spring Boot 4 modularizó las autoconfiguraciones y la de Flyway no se aplica
 * automáticamente con solo tener flyway-core en el classpath. Acá:
 *  1. Registramos el bean Flyway leyendo las propiedades spring.flyway.* habituales.
 *  2. Lo ejecutamos al inicializar el bean (migrate() vía initMethod).
 *  3. Forzamos a que el EntityManagerFactory dependa de Flyway, para que Hibernate
 *     valide el schema DESPUÉS de que Flyway haya creado las tablas.
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(
            DataSource dataSource,
            @Value("${spring.flyway.locations:classpath:db/migration}") String locations,
            @Value("${spring.flyway.baseline-on-migrate:false}") boolean baselineOnMigrate) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations.split(","))
                .baselineOnMigrate(baselineOnMigrate)
                .load();
    }

    /**
     * Garantiza el orden: Flyway corre antes de que Hibernate cree el EntityManagerFactory.
     * Sin esto, Hibernate puede intentar validar el schema antes de que Flyway aplique las
     * migraciones y todo falla con "missing table ...".
     */
    @Bean
    public static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlyway() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            for (String emfName : beanFactory.getBeanNamesForType(EntityManagerFactory.class, true, false)) {
                BeanDefinition bd = beanFactory.getBeanDefinition(emfName);
                Set<String> dependsOn = new LinkedHashSet<>();
                if (bd.getDependsOn() != null) {
                    dependsOn.addAll(Arrays.asList(bd.getDependsOn()));
                }
                dependsOn.add("flyway");
                bd.setDependsOn(dependsOn.toArray(new String[0]));
            }
        };
    }
}
