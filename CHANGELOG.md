# Changelog

## [4.1.0] - Unreleased

> **v4.0.0** was not released and has been skipped. The following is a summary of changes since v3.5.1.

### Breaking Changes

- **Java**: minimum version `17` → `21`
- **Spring Boot**: `3.5.9` → `4.1.0`
- **Jackson**: `com.fasterxml.jackson` → `tools.jackson` (3.x namespace migration)
- **Springdoc OpenAPI**: `2.8.15` → `3.0.3`
- **Swagger Annotations**: `swagger-annotations` → `swagger-annotations-jakarta`
- **`@Nullable`**: `jakarta.annotation.Nullable` → `org.jspecify.annotations.Nullable`
- **Spring Cloud**: `2025.0.1` → removed
- **OpenFeign**: removed `:febit-boot-feign`, `:febit-boot-feign-test`, `:febit-boot-starter-feign` submodules
- **Devkit**: `boot-devkit/` migrated to standalone repository (Flyway, jOOQ, Feign Plugin)

### Adaptations

- `ModelResolver`: use `JsonMapper.Builder` instead of `Jackson2ObjectMapperBuilder`
- `GenericTypeNameResolver`: adapt to Springdoc 3.x package path (`springdoc.swagger` subpackage)
- `ExtraEnumMetaPropertyCustomizer`: support Jackson 2.x / 3.x and classmate `ResolvedType` variants
- `ResponseResponseBodyAdvice`: broaden generic parameter to `Object`, replace with `instanceof` checks
- `JacksonCustomizer`: adapt to Jackson 3.x API
- `PaginationArgumentResolver`: adapt to new API, fix NPE on empty sort parameter

### New Features

- `boot-starter-jooq`: add `org.jooq:jooq-jpa-extensions` dependency

### Dependencies

- febit-commons 3.4.0 → 4.1.0
- febit-devkit 1.5.0 → 1.6.2
- Gradle 9.2.1 → 9.5.1
- springdoc-openapi-bom replaces dependency set

---

## [4.0.0] - Skipped (unreleased)
