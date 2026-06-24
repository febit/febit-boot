# Changelog

## [4.1.0] - Unreleased

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
