package com.bank.acs.service.lv;

import lombok.extern.slf4j.Slf4j;
import lv.ays.rid.RidSmartIdResponseDTO;
import lv.ays.rid.SimpleInterfaceRemote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.bank.acs.Profile.COUNTRY_LV_PROFILE;

// TODO: this is a temp solution in order to meet business needs with a limit amount of time, need a proper solution
@Slf4j
@Profile(COUNTRY_LV_PROFILE)
@Service
public class AuthStatusCheckThreadService {

    @Value("${smart-id.thread-cleaning.cleaning-interval-ms}")
    private int cleaningIntervalInMs;
    @Value("${smart-id.thread-cleaning.time-before-cleaning-inactive-ms}")
    private int timeBeforeCleaningInactiveInMs;
    @Value("${smart-id.thread-cleaning.time-before-cleaning-existing-ms}")
    private int timeBeforeCleaningExistingInMs;

    protected final SimpleInterfaceRemote simpleInterfaceRemote;

    private final static Map<String, SmartIdStatusCheckThread> threads = Collections.synchronizedMap(new HashMap());
    private static Thread cleanupThread = null;

    public AuthStatusCheckThreadService(SimpleInterfaceRemote simpleInterfaceRemote) {
        this.simpleInterfaceRemote = simpleInterfaceRemote;
    }

    public Optional<RidSmartIdResponseDTO> checkSmartIdStatus(String acsTransactionId, String username, String smartIdHash, String messageToUser) {
        if(!isAuthStatusCheckingThreadCreated(acsTransactionId)) {
            return startAuthStatusChecking(acsTransactionId, username, smartIdHash, messageToUser);
        } else if (!smartIdHash.equals(threads.get(acsTransactionId).getSmartIdHash())) {
            log.warn("Thread for acsTransactionId = {} auth status checking already exists with smartIdHash = {}", acsTransactionId, threads.get(acsTransactionId).getSmartIdHash());
            stopAuthStatusChecking(acsTransactionId);
            Optional<RidSmartIdResponseDTO> result = startAuthStatusChecking(acsTransactionId, username, smartIdHash, messageToUser);
            return result;
        } else {
            return getCurrentAuthStatus(acsTransactionId);
        }
    }

    public boolean isAuthStatusCheckingThreadCreated(String acsTransactionId) {
        return threads.get(acsTransactionId) != null;
    }

    public Optional<RidSmartIdResponseDTO> startAuthStatusChecking(String acsTransactionId, String username, String smartIdHash, String messageToUser) {
        threads.put(acsTransactionId, new SmartIdStatusCheckThread(acsTransactionId, username, smartIdHash, messageToUser));
        threads.get(acsTransactionId).start();
        log.info("Starting Smart-ID status checking thread for acsTransactionId = {}, smartIdHash = {}", acsTransactionId, smartIdHash);
        return Optional.empty();
    }

    public void stopAuthStatusChecking(String acsTransactionId) {
        log.info("Stopping Smart-ID status checking thread for acsTransactionId = {}, smartIdHash = {}", acsTransactionId, threads.get(acsTransactionId).getSmartIdHash());
        if (threads.get(acsTransactionId).isRunning()) {
            threads.get(acsTransactionId).stopStatusChecking();
        }
    }

    public Optional<RidSmartIdResponseDTO> getCurrentAuthStatus(String acsTransactionId) {
        log.info("Returning auth status checking result for acsTransactionId = {}, smartIdHash = {}", acsTransactionId, threads.get(acsTransactionId).getSmartIdHash());
        threads.get(acsTransactionId).setLastStatusCheckTime(System.currentTimeMillis());
        return threads.get(acsTransactionId).getSmartIdResult();
    }

    private void startCleanUpThread() {
        if(cleanupThread == null) {
            log.info("Registering cleanUp thread");
            cleanupThread = new Thread(() -> {
                while(true) {
                    try {
                        Thread.sleep(cleaningIntervalInMs);
                        cleanUp();
                    } catch (Exception e) {
                        log.error("Error while performing cleanUp {}", e.getMessage(), e);
                    }
                }
            });
            cleanupThread.start();
            log.info("CleanUp thread registered");
        }
    }

    private void cleanUp() {
        long runningThreadsBefore = threads.values().stream().filter(thread -> thread.isRunning()).count();
        if (!threads.isEmpty()) {
            log.info("Performing CleanUp - auth status checking threads: total={} (of them running={})", threads.size(), runningThreadsBefore);
        }

        for(Iterator<Map.Entry<String, SmartIdStatusCheckThread>> it = threads.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, SmartIdStatusCheckThread> entry = it.next();
            SmartIdStatusCheckThread statusCheckThread = entry.getValue();

            if (statusCheckThread.getStartTime() != null && statusCheckThread.getStartTime() + timeBeforeCleaningExistingInMs < System.currentTimeMillis()) {
                stopAuthStatusChecking(entry.getKey());
                it.remove();
            } else if (statusCheckThread.getLastStatusCheckTime() != null && statusCheckThread.getLastStatusCheckTime() + timeBeforeCleaningInactiveInMs < System.currentTimeMillis()) {
                stopAuthStatusChecking(entry.getKey());
                it.remove();
            }
        }

        long runningThreadsAfter = threads.values().stream().filter(thread -> thread.isRunning()).count();
        if (!threads.isEmpty()) {
            log.info("After cleanup - auth status checking threads: total={} (of them running={})", threads.size(), runningThreadsAfter);
        }
    }

    public class SmartIdStatusCheckThread extends Thread {

        private final AtomicBoolean running = new AtomicBoolean(false);
        private String acsTransactionId;
        private String username;
        private String smartIdHash;
        private String messageToUser;
        private Optional<RidSmartIdResponseDTO> smartIdResult = Optional.empty();
        private Long startTime;
        private Long lastStatusCheckTime;

        public SmartIdStatusCheckThread(String acsTransactionId, String username, String smartIdHash, String messageToUser) {
            this.acsTransactionId = acsTransactionId;
            this.username = username;
            this.smartIdHash = smartIdHash;
            this.messageToUser = messageToUser;
        }

        @Override
        public void run() {
            startCleanUpThread();
            running.set(true);
            startTime = System.currentTimeMillis();
            while (smartIdResult.isEmpty() && running.get()) {
                final RidSmartIdResponseDTO result = simpleInterfaceRemote.smartIdAuthCheck(username, smartIdHash, messageToUser);

                if (result != null && result.getStatus() != null) {
                    log.info("smartIdAuthCheck respond with hash = {} and status = {}, AcsTransactionId = {}", result.getHash(), result.getStatus(), acsTransactionId);
                    smartIdResult = Optional.of(result);
                } else {
                    log.info("smartIdAuthCheck respond with empty body/status, AcsTransactionId = {}", acsTransactionId);
                }
            }
            running.set(false);
        }

        public String getSmartIdHash() {
            return smartIdHash;
        }

        public Optional<RidSmartIdResponseDTO> getSmartIdResult() {
            return smartIdResult;
        }

        public void stopStatusChecking() {
            running.set(false);
        }

        public void setLastStatusCheckTime(Long lastStatusCheckTime) {
            this.lastStatusCheckTime = lastStatusCheckTime;
        }

        public Long getStartTime() {
            return startTime;
        }

        public Long getLastStatusCheckTime() {
            return lastStatusCheckTime;
        }

        public boolean isRunning() {
            return running.get();
        }
    }

}
