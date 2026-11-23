# Changelog

## [4.2.0-M2] - 2026-11-23

### Breaking Changes

- `JooqSearchFormPropertyCustomizer`：`x-search-filter` 改用 `@CriterionMapping.using()`（febit-commons 4.2.0-M2）。

### Changes

- `FebitSpringdocAutoConfiguration`：将 Spring Boot Jackson customizer 应用到 springdoc 的 `ModelResolver` mapper，使自定义
  mixin 在生成的 OpenAPI 文档中生效。
- `ExtraEnumMetaPropertyCustomizer`：移除 Jackson 2.x `ResolvedType` 兼容分支。
- 测试：覆盖 springdoc mixin / factory 定制、Web 条件装配及 Jackson 3 / swagger-core 上游行为。

### Build

- Gradle 9.7.1 → 9.9.1；Java 插件改用 `toolchain`。
- 仓库声明集中到 `settings.gradle`；模板从 `etc/` 移至 `gradle/`。

### Dependencies

- febit-commons 4.2.0-M1 → 4.2.0-M2
- Spring Boot 4.2.0-M2 → 4.2.0
- springdoc OpenAPI 3.1.1 → 3.2.1
- Swagger 3.0.0：改用 `swagger-annotations` / `swagger-core`，去掉 `-jakarta` 后缀。

## [4.2.0-M1] - 2026-10-04

### Breaking Changes

- 日志：文件追加器（`all` / `debug` / `info` / `warn`）改用 `StructuredLogEncoder` 输出 JSON Lines（`.jsonl`，ECS 格式），不再输出纯文本
  pattern。

### New Features

- `validation`：新增 `@EachSize` —— 针对 `Collection` / `Map` / 数组字段的逐元素尺寸约束。
- `validation`：新增 `@OversizeWarning` —— 仅日志告警、不拒绝请求的超大尺寸校验；阈值 `warn` / `error`（`-1` 表示该级别关闭）。

### Changes

- **`IModel` / `Models`**: 标记废弃，将在后续版本移除（自 `4.2.0` 起）

### Build

- 用 Spotless（license header、`cleanthat`、移除未用 import、缩短全限定名）替换 `com.github.hierynomus.license` Gradle 插件。

### Dependencies

- Spring Boot 4.1.x → 4.2.0-M2
- febit-commons 4.1.x → 4.2.0-M1
- febit-devkit 1.6.2 → 1.7.0

## [4.1.1] - 尚未发布

### Dependencies

- febit-commons 4.1.0 → 4.1.1
- Spring Boot 4.1.0 → 4.1.1
- Springdoc OpenAPI 3.0.3 → 3.1.1
- Gradle 9.5.1 → 9.7.1

## [4.1.0] - 2026-06-24

> **v4.0.0** 未发布，已跳过。以下为相对于 v3.5.1 的变更汇总。

### Breaking Changes

- **Java 最低版本**: `17` → `21`
- **Spring Boot**: `3.5.9` → `4.1.0`
- **Jackson**: `com.fasterxml.jackson` → `tools.jackson`（3.x 命名空间迁移）
- **Springdoc OpenAPI**: `2.8.15` → `3.0.3`
- **Swagger Annotations**: `swagger-annotations` → `swagger-annotations-jakarta`
- **`@Nullable`**: `jakarta.annotation.Nullable` → `org.jspecify.annotations.Nullable`
- **Spring Cloud**: `2025.0.1` → 已移除
- **OpenFeign**: 移除 `:febit-boot-feign`、`:febit-boot-feign-test`、`:febit-boot-starter-feign` 子模块
- **Devkit**: `boot-devkit/` 迁移到独立仓库（Flyway、jOOQ、Feign Plugin）

### Adaptations

- `ModelResolver`: 使用 `JsonMapper.Builder` 替代 `Jackson2ObjectMapperBuilder`
- `GenericTypeNameResolver`: 适配 Springdoc 3.x 包路径（`springdoc.swagger` 子包）
- `ExtraEnumMetaPropertyCustomizer`: 兼容 Jackson 2.x / 3.x 及 classmate 的多种 `ResolvedType`
- `ResponseResponseBodyAdvice`: 泛型参数放宽为 `Object`，实例化时 instanceof 检查替代
- `JacksonCustomizer`: 适配 Jackson 3.x API
- `PaginationArgumentResolver`: 适配新 API，修复空 sort 参数 NPE

### New Features

- `boot-starter-jooq` 新增 `org.jooq:jooq-jpa-extensions` 依赖

### Dependencies

- febit-commons 3.4.0 → 4.1.0
- febit-devkit 1.5.0 → 1.6.2
- Gradle 9.2.1 → 9.5.1
- springdoc-openapi-bom 替代 dependency set

---

## [4.0.0] - 未发布，已跳过
