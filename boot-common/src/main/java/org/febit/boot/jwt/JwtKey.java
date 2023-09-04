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
package org.febit.boot.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.febit.lang.Lazy;
import org.febit.lang.UncheckedException;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

@RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
public class JwtKey {

    @Getter
    private final String id;
    @Getter
    private final JwkAlgorithm algorithm;

    private final String verifierKey;
    private final String signerKey;

    private final Lazy<JWSSigner> signer = Lazy.of(this::createSigner);

    public JWSSigner signer() {
        return signer.get();
    }

    public JWSVerifier verifier(JWSHeader header) throws JOSEException {
        return new DefaultJWSVerifierFactory()
                .createJWSVerifier(header, verifierKey());
    }

    private JWSSigner createSigner() {
        var privateKey = signerKey();
        try {
            return algorithm.getSignerFactory().create(privateKey);
        } catch (JOSEException e) {
            throw new UncheckedException(e);
        }
    }

    public PublicKey verifierKey() {
        try {
            return algorithm.getSecurity().decodePublicKey(verifierKey);
        } catch (InvalidKeySpecException e) {
            throw new UncheckedException(e);
        }
    }

    public PrivateKey signerKey() {
        try {
            return algorithm.getSecurity().decodePrivateKey(signerKey);
        } catch (InvalidKeySpecException e) {
            throw new UncheckedException(e);
        }
    }
}
