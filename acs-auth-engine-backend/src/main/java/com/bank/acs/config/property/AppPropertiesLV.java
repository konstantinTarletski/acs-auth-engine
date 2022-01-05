package com.bank.acs.config.property;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.bank.acs.Profile.COUNTRY_LV_PROFILE;

@Getter
@Configuration
@Profile(COUNTRY_LV_PROFILE)
public class AppPropertiesLV {

    @Value("${app.smart-id.message-to-user-json-path}")
    private String smartIdMessageToUserJsonPath;

    @Value("${app.roof-id.hostname}")
    private String roofIdHostname;

    @Value("${app.roof-id.port}")
    private String roofIdPort;

    @Value("${app.roof-id.wsdl.path}")
    private String roofIdWsdlPath;

    public String getRoofIdUrl() {
        return roofIdHostname + ":" + roofIdPort;
    }

}
