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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.Getter;
import org.febit.boot.devkit.jooq.gradle.container.ContainerDatabaseJdbcProvider;
import org.febit.boot.devkit.jooq.gradle.embedded.EmbeddedPostgresJdbcProvider;
import org.febit.boot.devkit.jooq.meta.MetaUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.SchemaMappingType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class JooqCodegenExtension {

    static final String DIR_GENERATED_SRC = "generated/sources/" + MetaUtils.CODEGEN_JOOQ_FOLDER;
    private static final List<String> DEFAULT_MIGRATIONS_DIR = List.of("db/migration");

    @Getter
    private final Project project;

    /**
     * Location to scan recursively for migrations.
     * (default: db/migration)
     *
     * @see FluentConfiguration#locations(String...)
     */
    @Getter
    private final ListProperty<String> migrationsDirs;

    @Getter
    private final EmbeddedPostgresConfig embeddedPostgres = new EmbeddedPostgresConfig();

    @Getter
    private final Property<ContainerDbConfig> container;

    @Getter
    private final DirectoryProperty generatedSourceDir;

    @Getter
    private final Property<Generator> generator;

    @Getter
    private final Property<Jdbc> jdbc;

    @Getter
    private final Property<Configuration> jooqConfig;

    @Getter
    private final Property<JdbcProvider<?>> jdbcProvider;

    @Inject
    public JooqCodegenExtension(Project project) {
        this.project = project;
        var objects = project.getObjects();

        this.migrationsDirs = objects.listProperty(String.class);
        migrationsDirs.addAll(DEFAULT_MIGRATIONS_DIR);

        var buildDir = project.getLayout().getBuildDirectory();
        this.generatedSourceDir = objects.directoryProperty()
                .convention(buildDir.dir(DIR_GENERATED_SRC));

        var conf = new Configuration();
        conf.setJdbc(new Jdbc());
        conf.setGenerator(MetaUtils.createAndInitGenerator());
        conf.getGenerator().getDatabase()
                .withForcedTypes(new ArrayList<>());
        conf.getGenerator().getTarget()
                .withDirectory(DIR_GENERATED_SRC);

        this.jooqConfig = objects.property(Configuration.class)
                .convention(conf);

        this.jdbc = objects.property(Jdbc.class)
                .convention(this.jooqConfig.map(Configuration::getJdbc));
        this.generator = objects.property(Generator.class)
                .convention(this.jooqConfig.map(Configuration::getGenerator));

        @SuppressWarnings("unchecked")
        var cls = (Class<JdbcProvider<?>>) ((Class<?>) JdbcProvider.class);
        this.jdbcProvider = objects.property(cls)
                .convention(new PresetJdbcProvider(conf.getJdbc()));

        this.container = objects.property(ContainerDbConfig.class)
                .convention(new ContainerDbConfig());
    }

    public Provider<String> getTargetDir() {
        return this.generator.map(g ->
                g.getTarget().getDirectory()
        );
    }

    /**
     * Location to scan recursively for migrations.
     * (default: db/migration)
     *
     * @see #migrationsDir(String...)
     */
    public void migrationsDir(String dir) {
        this.migrationsDirs.add(dir);
    }

    /**
     * Location to scan recursively for migrations.
     * (default: db/migration)
     *
     * @see #migrationsDir(String)
     */
    public void migrationsDir(String... dirs) {
        this.migrationsDirs.addAll(dirs);
    }

    public void forcedTypes(@DelegatesTo(ForcedTypesHandler.class) Closure<?> closure) {
        var types = new ForcedTypesHandler();
        GradleUtils.to(closure, types);
        this.generator.get().getDatabase()
                .getForcedTypes().addAll(types);
    }

    public void setSchema(String schema) {
        this.generator.get().getDatabase()
                .setInputSchema(schema);
    }

    public void setIncludeSchema(String schema) {
        SchemaMappingType type = new SchemaMappingType();
        type.setInputSchema(schema);
        this.generator.get().getDatabase().getSchemata().add(type);
    }

    public void setExcludes(String excludes) {
        this.generator.get().getDatabase().setExcludes(excludes);
    }

    public void setTargetPackage(String name) {
        this.generator.get().getTarget().setPackageName(name);
    }

    public void setGenerate(@DelegatesTo(Generate.class) Closure<?> closure) {
        GradleUtils.to(closure, this.generator.get().getGenerate());
    }

    public void presetJdbc(@DelegatesTo(Jdbc.class) Closure<?> closure) {
        this.jdbcProvider.convention(new PresetJdbcProvider(this.jdbc.get()));
        GradleUtils.to(closure, this.jdbc.get());
    }

    public void embeddedPostgres(@DelegatesTo(EmbeddedPostgresConfig.class) Closure<?> closure) {
        embeddedPostgres();
        GradleUtils.to(closure, this.embeddedPostgres);
    }

    public void containerDatabase(@DelegatesTo(ContainerDbConfig.class) Closure<?> closure) {
        var conf = this.container.get();
        var provider = ContainerDatabaseJdbcProvider.builder()
                .conf(conf)
                .generator(this.generator.get())
                .buildDir(project.getLayout().getBuildDirectory().getAsFile().get())
                .build();
        this.jdbcProvider.convention(provider);
        GradleUtils.to(closure, conf);
    }

    public void embeddedPostgres() {
        EmbeddedPostgresJdbcProvider.prepare(project);
        var conf = this.embeddedPostgres;
        var provider = EmbeddedPostgresJdbcProvider.builder()
                .conf(conf)
                .buildDir(project.getLayout().getBuildDirectory().getAsFile().get())
                .build();
        this.jdbcProvider.convention(provider);
    }

}
