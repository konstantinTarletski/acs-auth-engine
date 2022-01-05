package com.bank.acs.backgroundttask;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.TransactionMonitorRepository;
import com.bank.acs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteInactiveSessionsBgTask {

    protected final AppSessionRepository appSessionRepository;
    protected final UserService userService;
    protected final AppProperties appProperties;
    protected final TransactionMonitorRepository transactionMonitorRepository;

    @Transactional
    @Scheduled(cron = "${app.bgtask.clean-inactive-sessions.cron}")
    public void deleteInactiveSessions() {

        final var userInactiveTime = appProperties.getCleanInactiveSessionsLimitInMinutes();
        final var inactiveTime = LocalDateTime.now().minusMinutes(userInactiveTime);
        final var inactiveSessions = appSessionRepository.findAllByUpdatedBefore(inactiveTime);
        if (inactiveSessions.size() > 0) {
            final var totalBefore = appSessionRepository.count();
            log.info("Going to delete {} inactive sessions because of user inactive time for {} minutes, total DB sessions = {}", inactiveSessions.size(), userInactiveTime, totalBefore);
            inactiveSessions.forEach(appSession -> {
                log.info("Going to logout and delete user session acsTransactionId = {}, state = {}", appSession.getAcsTransactionId(), appSession.getState());
                userService.deleteAppSession(appSession);

                final var unclosedTransactions = transactionMonitorRepository.findAllByAcsTransactionId(appSession.getAcsTransactionId());
                log.info("Going to delete {} unclosed transactions  = {}", unclosedTransactions.size(), appSession.getAcsTransactionId());
                transactionMonitorRepository.deleteAll(unclosedTransactions);
            });
            final var totalAfter = appSessionRepository.count();
            log.info("Deleting inactive sessions complete, sessions left = {}", totalAfter);
        }

    }

}
