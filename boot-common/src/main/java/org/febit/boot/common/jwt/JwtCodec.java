/*
 * Copyright 2022-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.boot.common.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.febit.boot.common.util.AuthErrors;
import org.febit.lang.protocol.IResponse;

import java.text.ParseException;
import java.util.Date;

@RequiredArgsConstructor
public class JwtCodec {

    private final JwtCodecProps props;

    public IResponse<JWTClaimsSet> decode(String token) {
        SignedJWT jwt;
        try {
            jwt = SignedJWT.parse(token);
        } catch (ParseException e) {
            return AuthErrors.INVALID_TOKEN
                    .response("invalid signed JWT format: " + e.getMessage());
        }

        var keyId = jwt.getHeader().getKeyID();
        var key = props.getKey(keyId);
        if (key == null) {
            return AuthErrors.INVALID_TOKEN
                    .response("missing key ID");
        }

        JWSVerifier verifier;
        try {
            verifier = key.verifier(jwt.getHeader());
        } catch (JOSEException e) {
            return AuthErrors.INVALID_TOKEN
                    .response("cannot resolve signing verifier: " + e.getMessage());
        }

        try {
            jwt.verify(verifier);
        } catch (JOSEException e) {
            return AuthErrors.INVALID_TOKEN
                    .response("failed pass signing verifier: " + e.getMessage());
        }

        JWTClaimsSet claims;
        try {
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            return AuthErrors.INVALID_TOKEN
                    .response("cannot get payload from token: " + e.getMessage());
        }

        var now = new Date();

        var expireAt = claims.getExpirationTime();
        if (expireAt == null || expireAt.before(now)) {
            throw AuthErrors.TOKEN_EXPIRED
                    .exception("token expired");
        }

        var notBefore = claims.getNotBeforeTime();
        if (notBefore != null && notBefore.after(now)) {
            throw AuthErrors.INVALID_TOKEN
                    .exception("token not effective yet");
        }
        return IResponse.success(claims);
    }

    public String encode(JWTClaimsSet payload) throws JOSEException {
        var key = props.signerKey();
        var signer = props.signer();
        var header = new JWSHeader.Builder(key.getAlgorithm().getJws())
                .keyID(key.getId())
                .build();

        var jwt = new SignedJWT(header, payload);
        jwt.sign(signer);
        return jwt.serialize();
    }

}
