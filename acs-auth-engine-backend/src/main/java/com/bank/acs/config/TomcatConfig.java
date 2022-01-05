package com.bank.acs.config;


import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class TomcatConfig {

    private static final String PROTOCOL = "AJP/1.3";

    @Value("${tomcat.ajp.enabled}")
    private boolean ajpEnabled;

    @Value("${tomcat.ajp.address}")
    private String ajpAddress;

    @Value("${tomcat.ajp.port}")
    private int ajpPort;

    @Value("${tomcat.ajp.secret-required}")
    private boolean secretRequired;

    @Value("${tomcat.ajp.secret}")
    private String secret;

    @Bean
    public TomcatServletWebServerFactory servletContainer() throws UnknownHostException {

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        if (ajpEnabled) {
            Connector connector = new Connector(PROTOCOL);
            connector.setScheme("http");
            connector.setPort(ajpPort);
            connector.setAllowTrace(false);

            final AbstractAjpProtocol protocol = (AbstractAjpProtocol) connector.getProtocolHandler();
            protocol.setAddress(InetAddress.getByName(ajpAddress));
            connector.setSecure(secretRequired);
            if (secretRequired) {
                protocol.setSecret(secret);
            } else {
                protocol.setSecretRequired(false);
            }

            tomcat.addAdditionalTomcatConnectors(connector);
        }

        return tomcat;
    }

}
