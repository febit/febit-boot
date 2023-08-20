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
package org.febit.boot.devkit.jooq.gradle;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.febit.lang.util.Maps;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@UtilityClass
public class ChecksumUtils {

    public static boolean verifyDir(File dir, Map<String, String> checksums) {
        if (!dir.exists()) {
            return checksums.isEmpty();
        }
        var files = FileUtils.listFiles(dir, null, true);
        if (files.isEmpty()) {
            return checksums.isEmpty();
        }
        if (files.size() != checksums.size()) {
            return false;
        }
        for (File file : files) {
            var key = file.getAbsolutePath();
            if (!checksums.containsKey(key)) {
                return false;
            }
            var checksum = checksum(file);
            if (!checksum.equals(checksums.get(key))) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, String> checksumDir(File dir) {
        if (!dir.exists()) {
            return Map.of();
        }
        var files = FileUtils.listFiles(dir, null, true);
        if (files.isEmpty()) {
            return Map.of();
        }
        return Maps.mapping(files,
                File::getAbsolutePath,
                ChecksumUtils::checksum
        );
    }

    public static String checksum(File file) {
        try {
            return Long.toHexString(
                    FileUtils.checksumCRC32(file)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
