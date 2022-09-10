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
package org.febit.devkit.maven.codegen.jooq;

import lombok.Data;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.febit.jooq.codegen.meta.ForcedTypes;
import org.febit.jooq.codegen.meta.MetaUtils;
import org.febit.jooq.codegen.meta.Schema;
import org.febit.lang.util.ArraysUtils;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;
import static org.jooq.codegen.GenerationTool.DEFAULT_TARGET_DIRECTORY;

@Mojo(
        name = "codegen",
        defaultPhase = GENERATE_SOURCES,
        requiresDependencyResolution = COMPILE_PLUS_RUNTIME,
        threadSafe = true
)
public class JooqCodegenPlugin extends AbstractMojo {

    @Parameter(
            property = "project",
            required = true,
            readonly = true
    )
    private MavenProject project;

    @Parameter(
            property = "codegen.jooq.basedir"
    )
    private String basedir;

    @Parameter(
            property = "codegen.jooq.skip"
    )
    private boolean skip;

    @Parameter(
            property = "codegen.jooq.timeToInstant",
            defaultValue = "true"
    )
    private boolean timeToInstant;

    /**
     * The JDBC settings.
     */
    @Parameter(
            required = true
    )
    private Jdbc jdbc;

    /**
     * The generate settings.
     */
    @Parameter
    private Generate generate;

    @Parameter
    private List<TableType> tableTypes;

    @Data
    public static class TableType {
        private String table;
        private List<ColumnType> columns;
    }

    @Data
    public static class ColumnType {
        private String column;
        private String type;
    }

    @Override
    public void execute() throws MojoExecutionException {
        var log = getLog();
        if (skip) {
            log.info("Skipped jOOQ Codegen");
            return;
        }
        var conf = resolveConf();
        if (log.isDebugEnabled()) {
            log.debug("Using config:\n" + conf);
        }

        generate(conf);

        var targetDir = conf.getGenerator().getTarget().getDirectory();
        log.info("Generated under folder: " + targetDir);

        project.addCompileSourceRoot(targetDir);
    }

    private static String columnExpr(String table, String col) {
        return ".*\\." + table + "\\." + col;
    }

    private List<ForcedType> forcedTypes() {
        var list = new ArrayList<ForcedType>();
        if (timeToInstant) {
            ForcedTypes.timeToInstant(list::add);
        }

        if (tableTypes != null) {
            for (TableType tableType : tableTypes) {
                var table = tableType.getTable();
                var columns = tableType.getColumns();
                if (columns == null) {
                    continue;
                }
                for (var column : columns) {
                    var expr = columnExpr(table, column.getColumn());
                    var schema = Schema.parse(column.getType());
                    ForcedTypes.to(list::add, expr, schema);
                }
            }
        }

        return list;
    }

    private Configuration resolveConf() {
        if (basedir == null) {
            basedir = project.getBasedir().getAbsolutePath();
        }
        if (generate == null) {
            generate = new Generate();
        }
        var generator = new Generator();
        generator.setGenerate(generate);
        MetaUtils.initGenerator(generator);

        var db = generator.getDatabase();
        db.setForcedTypes(forcedTypes());

        var target = generator.getTarget();
        if (target.getDirectory() == null) {
            target.setDirectory(DEFAULT_TARGET_DIRECTORY);
        }

        return new Configuration()
                .withBasedir(basedir)
                .withGenerator(generator)
                .withJdbc(jdbc)
                ;
    }

    private void generate(Configuration conf) throws MojoExecutionException {
        var thread = Thread.currentThread();
        var originClassloader = thread.getContextClassLoader();
        var codegenClassLoader = resolveCodegenClassLoader();

        try {
            thread.setContextClassLoader(codegenClassLoader);
            GenerationTool.generate(conf);
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to run jOOQ generation tool", ex);
        } finally {
            thread.setContextClassLoader(originClassloader);
            try {
                codegenClassLoader.close();
            } catch (Throwable e) {
                getLog().warn("Failed to close classloader.", e);
            }
        }
    }

    private URLClassLoader resolveCodegenClassLoader() throws MojoExecutionException {
        try {
            var urls = ArraysUtils.collect(
                    project.getCompileClasspathElements(),
                    URL[]::new,
                    JooqCodegenPlugin::toUrl
            );
            return new URLClassLoader(urls, getClass().getClassLoader());
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create classloader for jooq codegen.", e);
        }
    }

    private static URL toUrl(String path) {
        try {
            return new File(path).toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new UncheckedIOException("Failed to resolve URL: " + path, ex);
        }
    }
}
