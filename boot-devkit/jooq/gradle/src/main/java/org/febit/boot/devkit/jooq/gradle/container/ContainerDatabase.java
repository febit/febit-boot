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
package org.febit.boot.devkit.jooq.gradle.container;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.febit.common.exec.ExecUtils;
import org.febit.devkit.gradle.util.FileExtraUtils;
import org.febit.devkit.gradle.util.FolderUtils;
import org.febit.devkit.gradle.util.SocketPorts;
import org.febit.lang.Lazy;
import org.febit.lang.UncheckedException;
import org.febit.lang.util.Millis;
import org.febit.lang.util.Polling;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ContainerDatabase {

    private static final String YAML_FILE = "docker-compose.yml";
    private static final String ENV_FILE = ".env";
    private static final String DOCKER_BIN = "docker"; // maybe "podman"

    private static final String DOCKER_COMPOSE_YAML = """
            name: ${PROJECT_NAME:-febit-codegen}

            x-labels: &x-labels
              labels:
                org.febit.devkit.module: 'jooq-codegen'

            services:
              postgres:
                profiles:
                  - postgres
                <<: *x-labels
                image: ${DB_IMAGE}
                environment:
                  POSTGRES_USER: ${DB_USER}
                  POSTGRES_PASSWORD: ${DB_PASSWORD}
                  POSTGRES_DB: ${DB_DATABASE}
                ports:
                  - "${DB_PORT}:5432"
            #    volumes:
            #      - ./data/postgres/lib:/var/lib/postgresql
            #      - ./data/postgres/data:/var/lib/postgresql/data

              mysql:
                profiles:
                  - mysql
                <<: *x-labels
                image: ${DB_IMAGE}
                environment:
                  MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
                  MYSQL_DATABASE: ${DB_DATABASE}
                  MYSQL_USER: ${DB_USER}
                  MYSQL_PASSWORD: ${DB_PASSWORD}
                ports:
                  - "${DB_PORT}:3306"
            #    volumes:
            #      - ./data/mysql/lib:/var/lib/mysql
            #      - ./data/mysql/data:/var/lib/mysql/data
            """;

    private final String dockerBinPath;
    private final DbType type;
    private final ClassLoader classLoader;
    private final File workingDir;
    private final int port;
    private final Map<String, String> env;
    private final List<String> baseArgs;
    private final String jdbcUrl;

    private final String user;
    private final String password;

    private final AtomicReference<Future<Integer>> daemonRef = new AtomicReference<>();
    private final Lazy<Driver> driver = Lazy.of(this::loadDriver);

    public IOException handleException(ExecutionException e) {
        var cause = e.getCause();
        if (cause instanceof IOException) {
            return (IOException) cause;
        }
        return new IOException("Unexpected error", cause);
    }

    public synchronized void start() throws IOException {
        if (daemonRef.get() != null) {
            return;
        }

        // Prepare working dir
        FolderUtils.mkdirs(workingDir);
        FileExtraUtils.writeIfNotMatch(new File(workingDir, YAML_FILE), DOCKER_COMPOSE_YAML);
        var envContent = env.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
        FileExtraUtils.writeIfNotMatch(new File(workingDir, ENV_FILE), envContent);

        var watch = StopWatch.createStarted();

        // Pull image
        var pull = exec("pull", "--policy", "missing");
        // XXX: timeout?
        try {
            pull.get();
        } catch (ExecutionException e) {
            throw handleException(e);
        } catch (InterruptedException e) {
            throw new IOException("Pull database image: Interrupted", e);
        }
        log.info("Pulled database image, cost {} ms", watch.getTime(TimeUnit.MILLISECONDS));

        // Start container
        watch.reset();
        daemonRef.set(exec("up", "--remove-orphans"));

        // Ping database
        var pingFuture = Polling.create(() -> {
                    if (!isDaemonRunning()) {
                        throw new IOException("Container exited with code: " + daemonRef.get().get());
                    }
                    ping();
                    return true;
                })
                .completeIf(this::pingCompleteIf)
                .initialDelay(Duration.ofMillis(300))
                .delayInMillis(100)
                .timeoutInMillis(Millis.SECOND * 30)
                .executor(Executors.newSingleThreadExecutor())
                .poll();

        Polling.Context<Boolean> ping;
        try {
            ping = pingFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Database not ready: Interrupted", e);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Database not ready", cause);
        }

        if (!Boolean.TRUE.equals(ping.get())) {
            if (ping.hasError()) {
                throw new IOException("Database not ready: ", ping.lastError());
            }
            throw new IOException("Database not ready: " + (ping.isTimeout() ? "Timeout" : "Unknown"));
        }
        log.info("Started database, cost {} ms", watch.getTime(TimeUnit.MILLISECONDS));
    }

    private boolean pingCompleteIf(Polling.Context<Boolean> ctx) {
        if (!isDaemonRunning()) {
            return true;
        }
        if (!ctx.hasError()) {
            return true;
        }

        var ex = ctx.lastError();
        if (ex instanceof ExecutionException) {
            ex = ex.getCause();
        }
        if (!(ex instanceof SQLException)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isDaemonRunning() {
        var daemon = daemonRef.get();
        return daemon != null && !daemon.isDone();
    }

    public boolean isReady() {
        try {
            ping();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Driver loadDriver() {
        try {
            return (Driver) Class.forName(type.getDriverClassName(), true, classLoader)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException
                 | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new UncheckedException("Can't create driver: " + type.getDriverClassName(), e);
        }
    }

    public void ping() throws SQLException {
        var props = new Properties();
        props.put("user", user);
        props.put("password", password);

        try (
                var connection = driver.get().connect(jdbcUrl, props);
                var statement = connection.createStatement()
        ) {
            statement.execute("SELECT 1");
        }
    }

    private Future<Integer> exec(String... args) throws IOException {
        var cmd = new CommandLine(dockerBinPath);
        baseArgs.forEach(cmd::addArgument);
        for (var arg : args) {
            cmd.addArgument(arg);
        }
        if (log.isInfoEnabled()) {
            log.info("Run: {}", StringUtils.join(cmd.toStrings(), ' '));
        }
        var launcher = ExecUtils.launcher()
                .command(cmd)
                .workingDir(workingDir.toPath())
                .stderr(System.err)
                .stdout(System.out);
        return launcher.start();
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    @SuppressWarnings("NullableProblems")
    private static ContainerDatabase create(
            @lombok.NonNull DbType type,
            @lombok.NonNull File workingDir,
            @lombok.NonNull String user,
            @lombok.NonNull String password,
            @lombok.NonNull ClassLoader classLoader,
            int port,
            @Nullable String dockerBinPath,
            @Nullable String database,
            @Nullable String image
    ) {
        if (port == 0) {
            port = SocketPorts.detect();
        }
        if (image == null) {
            image = type.getImage();
        }
        if (dockerBinPath == null) {
            dockerBinPath = DOCKER_BIN;
        }
        if (database == null) {
            database = "codegen";
        }

        var projectName = "febit-codegen-jooq-" + workingDir.hashCode();
        log.info("Create container database, docker-compose: {}", projectName);

        var env = new HashMap<String, String>();
        env.put("PROJECT_NAME", projectName);
        env.put("DB_USER", user);
        env.put("DB_PASSWORD", password);
        env.put("DB_DATABASE", database);
        env.put("DB_PORT", String.valueOf(port));
        env.put("DB_IMAGE", image);

        var jdbcUrl = "jdbc:" + type.getJdbcPrefix() + "://localhost:" + port + "/" + database;
        var baseArgs = List.of(
                "compose",
                "--file", YAML_FILE,
                "--profile", type.getProfile(),
                "--env-file", ENV_FILE
        );

        return new ContainerDatabase(
                dockerBinPath,
                type,
                classLoader,
                workingDir,
                port,
                env,
                baseArgs,
                jdbcUrl,
                user,
                password
        );
    }

    public synchronized void close() {
        var daemon = daemonRef.get();
        if (daemon == null) {
            return;
        }
        try {
            exec("down").get();
            daemon.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        daemonRef.set(null);
    }
}
