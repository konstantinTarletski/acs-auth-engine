package com.bank.acs.service;

import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.exception.BusinessException;

import java.util.Optional;
import java.util.Set;

public interface UiRender<T> {

    T getInternetBankLoginPage(String language, Optional<BusinessException> error);

    T getChooseAuthMethodPage(String language, Set<AuthMethod> authMethods, Optional<BusinessException> error);

    T getAuthEnterCodeForCodeCalculatorPage(String language, Optional<BusinessException> error);

    T getSmartIdMSignatureCheckStatusPage(String language, String authorizationCode, AuthMethod authMethod, Optional<BusinessException> error);

    T getAuthSuccessPage(String language, String amountAndCurrency, String merchantName, Optional<BusinessException> error);

    T getFatalFailurePage(String language, BusinessException error);

    T getNonFatalFailurePage(String language, BusinessException error);

}
