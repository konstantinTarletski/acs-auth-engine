package com.bank.acs.backgroundttask;

import com.bank.acs.entity.AppSession;
import com.bank.acs.entity.banktron.BanktronData;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.BanktronRepository;
import com.bank.acs.service.LinkAppMockConfig;
import com.bank.acs.service.lt.BanktronService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static com.bank.acs.Profile.INT_TEST_PROFILE;
import static com.bank.acs.enumeration.AppState.CARD_CHECK_SUCCESSFUL;
import static com.bank.acs.enumeration.AppState.CHECK_CARD_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles({INT_TEST_PROFILE, COUNTRY_LT_PROFILE})
@SpringBootTest
@Import(LinkAppMockConfig.class)
class DeleteInactiveSessionsBgTaskIntegrationTest {

    @MockBean
    private BanktronService banktronService;

    @Autowired
    private AppSessionRepository appSessionRepository;

    @Autowired
    private BanktronRepository banktronRepository;

    @Autowired
    private DeleteInactiveSessionsBgTask instanceToTest;

    @Transactional
    @Test
    void testDeleteInactiveSessionsLongerThan15Minutes() {
        // given
        final var banktronData1 = BanktronData.builder().acsTransactionId("1").sessionToken("ACTIVE_SESSION_TOKEN").build();
        final var banktronData2 = BanktronData.builder().acsTransactionId("2").sessionToken("INACTIVE_SESSION_TOKEN").build();
        banktronRepository.saveAll(List.of(banktronData1, banktronData2));

        final var now = LocalDateTime.now();
        final var session1 = AppSession.builder().acsTransactionId("1").state(CARD_CHECK_SUCCESSFUL).build();
        final var session2 = AppSession.builder().acsTransactionId("2").state(CARD_CHECK_SUCCESSFUL).build();
        final var session3 = AppSession.builder().acsTransactionId("3").state(CHECK_CARD_STATUS).build();
        appSessionRepository.saveAll(List.of(session1, session2, session3));

        // Hibernate automatically overrides "created" for new entities, therefore we should set them separately
        session1.setCreated(now.minusMinutes(10));
        session2.setCreated(now.minusMinutes(20));
        session3.setCreated(now.minusMinutes(30));
        appSessionRepository.saveAll(List.of(session1, session2, session3));

        // when
        instanceToTest.deleteInactiveSessions();

        // then
        final Iterable<AppSession> actual = appSessionRepository.findAll();
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).isEqualTo(session1);

        verify(banktronService, never()).logout("ACTIVE_SESSION_TOKEN");
        verify(banktronService).logout("INACTIVE_SESSION_TOKEN");
    }
}
