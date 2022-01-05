package com.bank.acs.service;

import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static com.bank.acs.Profile.INT_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles({INT_TEST_PROFILE, COUNTRY_LT_PROFILE})
@Import(LinkAppMockConfig.class)
@SpringBootTest
class RenderServiceIntegrationTest {

    @Autowired
    private RenderHTMLService renderHTMLService;

    @Test
    void testRenderReactAppHtmlToString() {
        // when
        final var html = renderHTMLService.getReactAppHtml("creqValue", "acsTransactionId", Optional.of(new BusinessException(AcsErrorCode.GENERAL_EXCEPTION)));
        // then
        log.info("Generated ReactApp:\n{}", html);
        assertThat(html).isNotNull();
    }
}
