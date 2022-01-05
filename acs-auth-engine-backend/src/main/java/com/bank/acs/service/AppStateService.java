package com.bank.acs.service;

import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AppStateService {

    private final AppSessionRepository appSessionRepository;

    public void saveState(String acsTransId, AppState state) {
        appSessionRepository.findById(acsTransId).ifPresent(
                appSession -> {
                    appSession.setState(state);
                    appSessionRepository.save(appSession);
                }
        );
    }

    public void saveErrorState(String acsTransId, AppState state, AcsErrorCode errorCode) {
        appSessionRepository.findById(acsTransId).ifPresent(
                appSession -> {
                    appSession.setState(state);
                    appSession.setErrorCode(errorCode);
                    appSessionRepository.save(appSession);
                }
        );
    }

}
