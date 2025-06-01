package com.example.ecofood.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;


@Component
public class JwtUtil {
    private static final String SECRET_KEY = "v6Ui0LNGKzSZ8q+XMDLPI/oywvfdE2KOsn5GQFPMKjt9lCM7evCoRkCb0TB2OMg+";

    public static String generateToken(String email) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("Akina")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
        jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
        return jwsObject.serialize();
    }

    public static String getUsernameFromToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject();
    }

    public static boolean isTokenValid(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (expiration.before(new Date())) {
                return false;
            }

            JWSVerifier verifier = new MACVerifier(SECRET_KEY);
            return signedJWT.verify(verifier);

        } catch (Exception e) {
            System.out.println("Lỗi JwtUtil: " + e.getMessage());
            return false;
        }
    }
}
