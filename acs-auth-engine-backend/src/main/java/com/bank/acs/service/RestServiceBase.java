package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.function.Function;

import static com.bank.acs.util.CardUtil.maskSensitiveInformation;
import static com.bank.acs.util.StringUtil.truncate;

@Slf4j
@RequiredArgsConstructor
public abstract class RestServiceBase {

    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;
    protected final AppProperties appProperties;


    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestWrapper<T, E, R> {
        private HttpHeaders headers;
        private String url;
        private R data;
        private Class<T> responseDto;
        private Class<E> errorDto;
        private Function<E, AcsErrorCode> errorHandler;
    }

    public <T, E, R> T sendPost(RequestWrapper<T, E, R> requestWrapper, Function<E, BusinessException> errorHandler) {

        String url = requestWrapper.url;
        if(appProperties.isTruncateRequest()){
            url = truncate(requestWrapper.url, " ...");
        }

        log.info("Sending POST to url = {} data = {}", url, maskSensitiveInformation(requestWrapper.data != null ? requestWrapper.data.toString() : ""));
        final HttpEntity<MultiValueMap<String, String>> payload = new HttpEntity(requestWrapper.data, requestWrapper.headers);

        ResponseEntity<T> response;
        try {
            response = restTemplate.postForEntity(requestWrapper.url, payload, requestWrapper.responseDto);

            if (response.getStatusCode().is2xxSuccessful()) {
                String body = maskSensitiveInformation(response.getBody() != null ? response.getBody().toString() : "");
                if(appProperties.isTruncateResponse()){
                    body = truncate(body,"...");
                }
                log.info("Remote host {} respond with status = {} and body = {}", requestWrapper.url, response.getStatusCode(), body);
                return response.getBody();
            }

            log.error("ERROR for url = {} respond with status = {}", requestWrapper.url, response.getStatusCode());
            throw new BusinessException(AcsErrorCode.INTERNAL_SERVICE_IS_NOT_REACHABLE);

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.error("ERROR for url = {} respond with status = {} and error body = {}", requestWrapper.url, e.getStatusCode(), e.getResponseBodyAsString());
            E error;
            try {
                error = objectMapper.readValue(e.getResponseBodyAsString(), requestWrapper.errorDto);
            } catch (Exception ej) {
                log.error("ERROR while reading response for endpoint {}: respond with error message = {}, cause = {}",
                        requestWrapper.url, ej.getMessage(), ej.getCause());
                throw new BusinessException(AcsErrorCode.INTERNAL_SERVICE_IS_NOT_REACHABLE);
            }
            log.error("ERROR for endpoint {}: Error = {}", requestWrapper.url, error);
            throw errorHandler.apply(error);
        }
    }

    protected HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        final var xRequestId = UUID.randomUUID().toString();
        headers.set(X_REQUEST_ID_HEADER, xRequestId);
        log.info("X-Request-ID = {}", xRequestId);
        return headers;
    }

}
