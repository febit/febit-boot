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

import com.nimbusds.jose.JWSSigner;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.febit.lang.util.Lazy;
import org.febit.lang.util.Maps;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
public class JwtCodecProps {

    @Getter
    private final String signerKeyId;
    @Getter
    private final List<JwtKey> keys;

    private final Lazy<Map<String, JwtKey>> mapping = Lazy.of(() -> Maps.mapping(this.getKeys(), JwtKey::getId));
    private final Lazy<JwtKey> signerKey = Lazy.of(this::singerKey);

    @Nullable
    public JwtKey getKey(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return mapping.get().get(id);
    }

    public JwtKey signerKey() {
        return signerKey.get();
    }

    public JWSSigner signer() {
        return this.signerKey.get().signer();
    }

    private JwtKey singerKey() {
        var key = mapping.get().get(signerKeyId);
        if (key == null) {
            throw new IllegalStateException("No signer key found for key id: " + signerKeyId);
        }
        return key;
    }

}
