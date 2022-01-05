package com.bank.acs.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@Slf4j
public class H2Configuration {

    @Value("${spring.datasource.port}")
    private String h2port;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseServer() throws SQLException {
        log.info("H2 server created");
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2port);
    }

}
