package com.bank.acs.service.ciam.jw;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface JWKSupport {

    PublicKey getPublicKey(String kid);

    PrivateKey getPrivateKey(String kid);

}
