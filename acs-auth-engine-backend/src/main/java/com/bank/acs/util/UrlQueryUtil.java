package com.bank.acs.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class UrlQueryUtil {

    public static String convertToQueryString(Map<String, String> params) {
        if(params != null && !params.isEmpty()){
            log.info("convertToQueryString, paramSize = {}", params.size());
            return params.entrySet().stream()
                    .map(p -> p.getKey() + "=" + p.getValue())
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");
        }
        log.info("convertToQueryString, params are empty");
        return "";
    }

    public static Map<String, String> parseQueryParams(String queryParamsRaw) {
        log.info("parseQueryParams value = {}", queryParamsRaw);
        if (queryParamsRaw == null || queryParamsRaw.isBlank()) {
            log.info("parseQueryParams queryParamsRaw is empty");
            return emptyMap();
        }
        final var ret = Arrays.stream(queryParamsRaw.split("&"))
                .map(part -> {
                    final String[] split = part.split("=");
                    final String key = split[0];
                    final String value = split.length > 1 ? UriUtils.decode(split[1], UTF_8) : null;
                    return Pair.of(key, value);
                })
                .filter(pair -> nonNull(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));

        log.info("queryParams = {}", ret);
        return ret;
    }

    public static String resourceToString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
