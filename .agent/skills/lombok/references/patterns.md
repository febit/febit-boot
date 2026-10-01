# Lombok 代码范式（patterns）

> 本文件是 [../SKILL.md](../SKILL.md) 各「范式 N」的完整代码；SKILL.md 只给决策表、速查与红线（判据）。
> 各范式的「为何这样写」见 SKILL.md 对应小节。

## 范式 1：不可变枚举

```java

@lombok.Getter
@lombok.RequiredArgsConstructor
public enum XxxType {
  BASIC("basic"),
  ALTERNATIVE("alternative"),
  ;
  private final String value;
}
```

## 范式 2：可变 DTO / VO（Form / VO）

```java

@lombok.Getter
@lombok.Setter
public class XxxForm {
  @NotNull
  @Positive
  private Long spaceId;
  @NotBlank
  @Size(max = 128)
  private String name;
  @Nullable
  private Visibility visibility;
  // …其它 @Nullable 可选字段
}
```

## 范式 3：不可变配置（私有构造器 + 工厂）

```java

@Accessors(fluent = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Builder(
    builderClassName = "InternalBuilder",
    builderMethodName = "internalBuilder",
    access = AccessLevel.PRIVATE)
public record XxxOptions(@lombok.NonNull String name, @lombok.NonNull ConnectionFactory connectionFactory) {
  // …其余 @NonNull final 字段
}
```

## 范式 4：纯静态工具类

```java

@UtilityClass
public class XxxUtils {
  public static final String PREFIX = "x-xxx-";

  public static void validate(Map<String, @Nullable Object> headers) { /* … */ }
}
```

## 范式 5：不可变状态记录

```java
public record XxxState(
    int retries,
    int failures,
    @lombok.With int fallback,
    @lombok.With @Nullable String lastError,
    @lombok.With long lastAttemptAt
) {
}
```
