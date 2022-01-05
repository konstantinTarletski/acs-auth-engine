package com.bank.acs.backgroundttask;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Component
public class LogoutInactiveUsersBgTask {

    protected final AppSessionRepository appSessionRepository;
    protected final UserService userService;
    protected final AppProperties appProperties;

    @Transactional
    @Scheduled(cron = "${app.bgtask.logout-inactive-users.cron}")
    public void logoutInactiveUsers() {

        final var userInactiveTime = appProperties.getLogoutInactiveUsersLimitInMinutes();
        final var inactiveTime = LocalDateTime.now().minusMinutes(userInactiveTime);
        final var inactiveSessions = appSessionRepository.findAllByUpdatedBefore(inactiveTime);
        final var needLogout = inactiveSessions.stream().filter(session -> session.getLogoutDone() != null && !session.getLogoutDone()).collect(toList());
        if (needLogout.size() > 0) {
            final var total = appSessionRepository.count();
            log.info("Going to logout {} users because of inactive time for {} minutes, sessions total DB DB = {}", inactiveSessions.size(), userInactiveTime, total);
            needLogout.forEach(appSession -> {
                try {
                    log.info("Logout user session acsTransactionId = {}, lastUpdated ={}, state = {}", appSession.getAcsTransactionId(), appSession.getUpdated(), appSession.getState());
                    if(appSession.getLogoutDone() != null && !appSession.getLogoutDone()){
                        userService.logout(appSession);
                    }
                } catch (Exception e) {
                    log.info("Error while logout user for acsTransactionId {}, state = {}", appSession.getAcsTransactionId(), appSession.getState(), e);
                }
            });
            log.info("Logout inactive users complete");
        }
    }
}
