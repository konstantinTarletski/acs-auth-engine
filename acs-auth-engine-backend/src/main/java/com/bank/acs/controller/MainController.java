package com.bank.acs.controller;

import com.bank.acs.repository.AppSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class MainController {

    protected final AppSessionRepository appSessionRepository;
    protected final ObjectMapper objectMapper;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @SneakyThrows
    public ResponseEntity<?> mainPage() {
        return ResponseEntity.ok(objectMapper.writeValueAsString(
                List.of("status", "ok", "appSessionsInDatabase", appSessionRepository.count()))
        );
    }

}
