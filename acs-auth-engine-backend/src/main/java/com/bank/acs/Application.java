package com.bank.acs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaRepositories("com.bank.acs.repository")
@EnableJpaAuditing
@SpringBootApplication
@PropertySource(value = "${app.config.location.development.global}", ignoreResourceNotFound = true)
@PropertySource(value = "${app.config.location.development.country}", ignoreResourceNotFound = true)
@PropertySource(value = "${app.config.location.global}", ignoreResourceNotFound = true)
@PropertySource(value = "${app.config.location.country}", ignoreResourceNotFound = true)
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
