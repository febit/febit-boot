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
package org.febit.boot.common.util.security;

import lombok.RequiredArgsConstructor;
import org.febit.lang.UncheckedException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RequiredArgsConstructor
public enum SecurityAlgorithm {

    EC(Algorithms.EC,
            encoded -> genericPublicKey(Algorithms.EC, encoded),
            encoded -> genericPrivateKey(Algorithms.EC, encoded)
    ),
    RSA(Algorithms.RSA,
            encoded -> genericPublicKey(Algorithms.RSA, encoded),
            encoded -> genericPrivateKey(Algorithms.RSA, encoded)
    ),
    ;

    private final String algorithm;
    private final KeyDecoder<PublicKey> publicKeyDecoder;
    private final KeyDecoder<PrivateKey> privateKeyDecoder;

    public PublicKey decodePublicKey(String base64) throws InvalidKeySpecException {
        try {
            return publicKeyDecoder.decode(base64);
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedException(e);
        }
    }

    public PrivateKey decodePrivateKey(String base64) throws InvalidKeySpecException {
        try {
            return privateKeyDecoder.decode(base64);
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedException(e);
        }
    }

    private interface KeyDecoder<T extends Key> {
        T decode(String encoded) throws NoSuchAlgorithmException, InvalidKeySpecException;
    }

    private static byte[] decodeBase64(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    public static PublicKey genericPublicKey(String alg, String encoded)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        var keyFactory = KeyFactory.getInstance(alg);
        var publicKeySpec = new X509EncodedKeySpec(
                decodeBase64(encoded)
        );
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static PrivateKey genericPrivateKey(String alg, String encoded)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        var keyFactory = KeyFactory.getInstance(alg);
        var keySpec = new PKCS8EncodedKeySpec(
                decodeBase64(encoded)
        );
        return keyFactory.generatePrivate(keySpec);
    }
}
