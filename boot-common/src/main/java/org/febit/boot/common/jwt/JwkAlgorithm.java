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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.febit.boot.common.util.security.SecurityAlgorithm;

import java.security.Key;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;

@RequiredArgsConstructor
public enum JwkAlgorithm {

    RS256(
            JWSAlgorithm.RS256,
            SecurityAlgorithm.RSA,
            RSASSASigner::new
    ),
    RS384(
            JWSAlgorithm.RS384,
            SecurityAlgorithm.RSA,
            RSASSASigner::new
    ),
    RS512(
            JWSAlgorithm.RS512,
            SecurityAlgorithm.RSA,
            RSASSASigner::new
    ),

    ES256(
            JWSAlgorithm.ES256,
            SecurityAlgorithm.EC,
            JwkAlgorithm::ecdsaSigner
    ),
    ES256K(
            JWSAlgorithm.ES256K,
            SecurityAlgorithm.EC,
            JwkAlgorithm::ecdsaSigner
    ),
    ES384(
            JWSAlgorithm.ES384,
            SecurityAlgorithm.EC,
            JwkAlgorithm::ecdsaSigner
    ),
    ES512(
            JWSAlgorithm.ES512,
            SecurityAlgorithm.EC,
            JwkAlgorithm::ecdsaSigner
    ),
    ;

    @Getter
    private final JWSAlgorithm jws;
    @Getter
    private final SecurityAlgorithm security;
    @Getter
    private final Factory<PrivateKey, JWSSigner> signerFactory;

    @FunctionalInterface
    public interface Factory<K extends Key, J> {
        J create(K key) throws JOSEException;
    }

    private static ECDSASigner ecdsaSigner(PrivateKey key) throws JOSEException {
        return new ECDSASigner((ECPrivateKey) key);
    }
}
