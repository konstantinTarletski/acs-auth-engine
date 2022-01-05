package com.bank.acs.config;

import com.bank.acs.util.ObjectToUrlEncodedConverter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .messageConverters(
                        //TODO Try to use standard one.
                        new ObjectToUrlEncodedConverter(),
                        new MappingJackson2HttpMessageConverterCustom())
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }

    public static class MappingJackson2HttpMessageConverterCustom extends MappingJackson2HttpMessageConverter {

        @Override
        public boolean canRead(Class<?> clazz, MediaType mediaType) {
            return mediaType == null || !MediaType.APPLICATION_FORM_URLENCODED.toString().equals(mediaType.toString());

        }

        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return mediaType == null || !MediaType.APPLICATION_FORM_URLENCODED.toString().equals(mediaType.toString());
        }
    }


}
