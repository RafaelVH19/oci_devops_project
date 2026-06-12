package com.springboot.MyTodoList.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates Better Auth EdDSA (Ed25519) JWTs.
 *
 * Nimbus' standard JWSVerificationKeySelector cannot be used for OKP/Ed25519 keys:
 * it converts JWKs to java.security.Key via OctetKeyPair.toPublicKey(), which
 * unconditionally throws "Export to java.security.PublicKey not supported". The
 * exception is swallowed, the key list comes back empty, and DefaultJWTProcessor
 * fails with "Another algorithm expected, or no matching key(s) found" even though
 * the kid matches. This decoder selects the OKP JWK by kid and verifies the
 * signature with the JDK's native Ed25519 implementation (available since Java 15).
 */
public class EdDSAJwtDecoder implements JwtDecoder {

    // X.509 SubjectPublicKeyInfo prefix for a raw Ed25519 public key (OID 1.3.101.112)
    private static final byte[] ED25519_SPKI_PREFIX = {
            0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00
    };

    private final JWKSource<SecurityContext> jwkSource;

    public EdDSAJwtDecoder(JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = jwkSource;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            OctetKeyPair okp = selectKey(jwt);
            if (!verifySignature(jwt, okp)) {
                throw new BadJwtException("JWT signature does not match JWKS key");
            }

            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            Instant now = Instant.now();
            Date exp = claimsSet.getExpirationTime();
            if (exp != null && now.isAfter(exp.toInstant())) {
                throw new BadJwtException("JWT is expired");
            }
            Date nbf = claimsSet.getNotBeforeTime();
            if (nbf != null && now.isBefore(nbf.toInstant())) {
                throw new BadJwtException("JWT is not yet valid");
            }

            Map<String, Object> claims = new HashMap<>(claimsSet.getClaims());
            claims.replaceAll((k, v) -> v instanceof Date d ? d.toInstant() : v);

            return new Jwt(
                    token,
                    claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : null,
                    exp != null ? exp.toInstant() : null,
                    jwt.getHeader().toJSONObject(),
                    claims
            );
        } catch (BadJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new BadJwtException("Failed to validate JWT: " + e.getMessage(), e);
        }
    }

    private OctetKeyPair selectKey(SignedJWT jwt) throws Exception {
        String kid = jwt.getHeader().getKeyID();
        JWKMatcher matcher = new JWKMatcher.Builder()
                .keyType(KeyType.OKP)
                .keyID(kid)
                .build();
        List<JWK> matches = jwkSource.get(new JWKSelector(matcher), null);
        if (matches == null || matches.isEmpty()) {
            throw new BadJwtException("No OKP key in JWKS matches kid " + kid);
        }
        return ((OctetKeyPair) matches.get(0)).toPublicJWK();
    }

    private boolean verifySignature(SignedJWT jwt, OctetKeyPair okp) throws Exception {
        byte[] rawKey = okp.getX().decode();
        byte[] spki = new byte[ED25519_SPKI_PREFIX.length + rawKey.length];
        System.arraycopy(ED25519_SPKI_PREFIX, 0, spki, 0, ED25519_SPKI_PREFIX.length);
        System.arraycopy(rawKey, 0, spki, ED25519_SPKI_PREFIX.length, rawKey.length);

        PublicKey publicKey = KeyFactory.getInstance("Ed25519")
                .generatePublic(new X509EncodedKeySpec(spki));

        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(publicKey);
        verifier.update(jwt.getSigningInput());
        return verifier.verify(jwt.getSignature().decode());
    }
}
