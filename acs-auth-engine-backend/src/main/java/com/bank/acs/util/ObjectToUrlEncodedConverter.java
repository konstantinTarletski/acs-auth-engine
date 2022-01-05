package com.bank.acs.util;

import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.exception.BusinessException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.bank.acs.util.UrlQueryUtil.convertToQueryString;

@NoArgsConstructor
@Slf4j
public class ObjectToUrlEncodedConverter implements HttpMessageConverter {
    private static final String Encoding = "UTF-8";


    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return mediaType != null && MediaType.APPLICATION_FORM_URLENCODED.toString().equals(mediaType.toString());
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        return getSupportedMediaTypes().contains(mediaType);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    public Object read(Class clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException, IOException {
        return new String(inputMessage.getBody().readAllBytes(), Encoding);
    }

    @Override
    public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException, IOException {
        if (o != null && o instanceof Map) {
            String body = convertToQueryString((Map) o);
            outputMessage.getBody().write(body.getBytes(Encoding));
        }else {
            log.warn("Unsupported Object type {}", o.getClass());
            throw  new BusinessException(AcsErrorCode.GENERAL_EXCEPTION);
        }
    }

}