package com.bank.acs.config.property;

import com.bank.acs.enumeration.AuthMethod;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Getter
@Configuration
public class AppProperties {

    @Value("${app.application-host-base-url}")
    private String applicationHostBaseUrl;

    @Value("${app.api.challenge.url}")
    private String challengeApi;

    @Value("${app.link-app.url}")
    private String linkAppUrl;

    @Value("${app.link-app.wsdl.path}")
    private String linkAppWsdlPath;

    @Value("${app.language.default}")
    private String defaultLanguage;

    @Value("${app.language.allowed}")
    private Set<String> allowedLanguages;

    @Value("${app.auth-method.allowed}")
    private Set<AuthMethod> allowedAuthMethods;

    @Value("${app.translations-path}")
    private String translationsPath;

    @Value("${app.translations-filename}")
    private String translationsFilename;

    @Value("${app.bgtask.clean-inactive-sessions.limit-in-minutes}")
    private int cleanInactiveSessionsLimitInMinutes;

    @Value("${app.bgtask.logout-inactive-users.limit-in-minutes}")
    private int logoutInactiveUsersLimitInMinutes;

    @Value("${app.api.id-card-redirect.url}")
    private String idCardRedirectUrl;

    @Value("${http.base.request.truncate:false}")
    private boolean truncateRequest;

    @Value("${http.base.response.truncate:false}")
    private boolean truncateResponse;

}
