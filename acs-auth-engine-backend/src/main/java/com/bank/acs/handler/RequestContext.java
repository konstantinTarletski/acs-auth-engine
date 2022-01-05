package com.bank.acs.handler;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Data
@Component
@RequestScope
public class RequestContext<T> {

    private T object;
    private UUID id;

}
