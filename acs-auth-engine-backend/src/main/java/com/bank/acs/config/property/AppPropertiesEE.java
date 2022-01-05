package com.bank.acs.config.property;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;

@Getter
@Configuration
@Profile(COUNTRY_EE_PROFILE)
public class AppPropertiesEE {

    @Value("${app.ciam.access-token.url}")
    private String ciamAccessTokenUrl;

    @Value("${app.ciam.authenticate.url}")
    private String ciamAuthenticateUrl;

    @Value("${app.ciam.jwk-uri.url}")
    private String ciamJwkUriUrl;

    @Value("${app.ciam.logout.url}")
    private String ciamLogoutUrl;

    @Value("${app.ciam.sts.aud}")
    private String ciamStsAud;

    @Value("${app.ciam.aud}")
    private String ciamAud;

    @Value("${app.ciam.realm-value}")
    private String realmValue;

    @Value("${app.ciam.sts-realm-value}")
    private String stsRealmValue;

    @Value("${app.ciam.o-auth-client-id}")
    private String oAuthClientId;

    @Value("${app.ciam.sts-o-auth-client-id}")
    private String stsOAuthClientId;

    @Value("${app.ciam.client-assertion-type-jwt-bearer}")
    private String clientAssertionTypeJwtBearer;

    @Value("${app.ciam.enc-kid}")
    private String ciamEncKid;

    @Value("${app.ciam.token-ttl}")
    private Integer ciamTokenTtl;

    @Value("${app.ccc.url}")
    private String cccUrl;

    @Value("${app.ccc.authorization-header}")
    private String cccAuthorizationHeader;

    @Value("${app.ciam.id-card-redirect.url}")
    private String ciamIdCardRedirectUrl;

    @Value(value = "${app.ciam.sts-rsa-private-key.path}")
    private Resource ciamStsRsaPrivateKey;

    @Value(value = "${app.ciam.rsa-private-key.path}")
    private Resource ciamRsaPrivateKey;

    @Value("${server.servlet.context-path}")
    private String contextPath;

}
