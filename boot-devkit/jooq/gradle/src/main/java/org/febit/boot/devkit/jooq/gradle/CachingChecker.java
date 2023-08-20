package org.febit.boot.devkit.jooq.gradle;

import com.fasterxml.jackson.databind.JavaType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.Lists;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.febit.boot.devkit.jooq.gradle.JooqCodegenExtension.DEFAULT_MIGRATIONS_DIR;
import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;

@RequiredArgsConstructor(
        staticName = "create"
)
public class CachingChecker {

    private static final String CHECK_FILE = "codegen-jooq-checksums.json";

    private static final JavaType CHECKSUMS_TYPE = TYPE_FACTORY.constructMapType(
            HashMap.class,
            TYPE_FACTORY.constructType(String.class),
            TYPE_FACTORY.constructMapType(HashMap.class, String.class, String.class)
    );

    private final Project project;

    private File file(String path) {
        return project.file(path);
    }

    private File resourceFile(String path) {
        // TODO: hard code
        return project.file("src/main/resources/" + path);
    }

    public boolean hasUpdated() {
        var checksums = checksums();
        if (checksums.isEmpty()) {
            return true;
        }
        var dirs = getDirsToRecord();
        if (dirs.size() != checksums.size()) {
            return true;
        }
        for (File dir : dirs) {
            var key = dir.getAbsolutePath();
            if (!checksums.containsKey(key)) {
                return true;
            }
            if (!ChecksumUtils.verifyDir(dir, checksums.get(key))) {
                return true;
            }
        }
        return false;
    }

    public void update() {
        var dirs = getDirsToRecord();
        var checksums = new TreeMap<String, Map<String, String>>();
        for (File dir : dirs) {
            checksums.put(
                    dir.getAbsolutePath(),
                    ChecksumUtils.checksumDir(dir)
            );
        }

        var json = JacksonUtils.toJsonString(checksums);
        try {
            FileUtils.write(checksumsFile(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<File> getDirsToRecord() {
        var dirs = new ArrayList<File>();
        var extension = project.getExtensions()
                .getByType(JooqCodegenExtension.class);

        var migrationsDirs = extension.getMigrationsDirs();
        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        dirs.addAll(Lists.collect(migrationsDirs, this::resourceFile));
        if (migrationsDirs.isEmpty()) {
            dirs.addAll(Lists.collect(DEFAULT_MIGRATIONS_DIR, this::resourceFile));
        }

        dirs.add(file(JooqCodegenPlugin.DIR_GENERATED_SRC));
        return dirs;
    }

    private File checksumsFile() {
        return new File(project.getBuildDir(), CHECK_FILE);
    }

    private Map<String, Map<String, String>> checksums() {
        var file = checksumsFile();
        if (!file.exists()) {
            return Map.of();
        }
        try {
            var json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return JacksonUtils.parse(json, CHECKSUMS_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
