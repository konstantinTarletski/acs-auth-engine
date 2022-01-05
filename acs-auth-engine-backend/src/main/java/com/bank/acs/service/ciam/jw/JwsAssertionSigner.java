package com.bank.acs.service.ciam.jw;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@NoArgsConstructor
public class JwsAssertionSigner {

    @SneakyThrows
    public static RSAPrivateKey getRSAPrivateKey(String privateKeyContent) {
        Reader reader = new StringReader(privateKeyContent);
        PEMParser pemParser = new PEMParser(reader);
        Object privateKeyObject = pemParser.readObject();

        if (privateKeyObject == null) {
            throw new InvalidKeySpecException("Invalid private key format");
        }

        PEMKeyPair pair = (PEMKeyPair) privateKeyObject;
        byte[] encodedPrivateKey = pair.getPrivateKeyInfo().getEncoded();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    public static String newJws(long lifetime, Map<String, Object> claimsMap, PrivateKey privateKey) {
        return rsaSignedJWT(lifetime, claimsMap, privateKey).serialize();
    }

    private static SignedJWT rsaSignedJWT(long lifetime, Map<String, Object> claimsMap, PrivateKey privateKey) {

        try {
            JWTClaimsSet claimsSet = getJwtBuilder(lifetime, claimsMap).build();
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
            JWSSigner signer = new RSASSASigner(privateKey);
            SignedJWT jws = new SignedJWT(header, claimsSet);
            jws.sign(signer);
            return jws;
        } catch (JOSEException e) {
            throw new RuntimeException("ERR_JWT_SIGN_EXCEPTION", e);
        }
    }

    private static JWTClaimsSet.Builder getJwtBuilder(long lifetime, Map<String, Object> claimsMap) {

        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + lifetime;

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.issueTime(new Date(nowMillis));
        builder.expirationTime(new Date(expMillis));
        builder.notBeforeTime(new Date());
        builder.jwtID(UUID.randomUUID().toString());

        claimsMap.forEach(builder::claim);
        if (claimsMap.get("aud") != null) {
            builder.audience(claimsMap.get("aud").toString());
        }

        return builder;
    }
}