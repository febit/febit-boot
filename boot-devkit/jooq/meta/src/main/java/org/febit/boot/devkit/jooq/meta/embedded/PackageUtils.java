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
package org.febit.boot.devkit.jooq.meta.embedded;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SystemUtils;

import static org.apache.commons.lang3.StringUtils.lowerCase;

@UtilityClass
public class PackageUtils {

    public static OS os() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return OS.WINDOWS;
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            return OS.MAC_OSX;
        }
        if (SystemUtils.IS_OS_LINUX) {
            return OS.LINUX;
        }
        throw new IllegalStateException("Unsupported OS: " + SystemUtils.OS_NAME);
    }

    public static Arch arch() {
        var arch = lowerCase(SystemUtils.OS_ARCH)
                .replaceAll("[^a-z0-9]+", "");
        return switch (arch) {
            case "x8664",
                    "amd64",
                    "ia32e",
                    "em64t",
                    "x64",
                    "aarch64" -> Arch.AMD64;
            case "x8632",
                    "x86",
                    "i386",
                    "i486",
                    "i586",
                    "i686",
                    "ia32",
                    "x32" -> Arch.I386;
            default -> throw new IllegalStateException("Unsupported arch: " + arch);
        };
    }

    @Getter
    @RequiredArgsConstructor
    public enum OS {
        MAC_OSX("darwin"),
        LINUX("linux"),
        WINDOWS("windows"),
        ;

        private final String ident;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Arch {
        I386("i386"),
        AMD64("amd64"),
        ;

        private final String artifact;
    }

    @UtilityClass
    public static class Postgres {

        public static String artifact() {
            return String.format("io.zonky.test.postgres:embedded-postgres-binaries-%s-%s",
                    os().getIdent(),
                    arch().getArtifact());
        }
    }

}
