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
