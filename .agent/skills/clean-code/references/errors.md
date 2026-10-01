# 错误文案范式（errors）

> 本文件给 **错误码 / 错误文案的正向范式**；红线声明见 SKILL.md 入口。
> 统一响应与错误码用法见

## 原则

错误码是 **稳定契约**：枚举名即契约，调用方按它判定 → 改名即破坏性变更。
文案是用户体验，不进异常、不硬编码在枚举里、不走裸字符串。

## Java（`IResponse` / `IErrors`）

范式（来自 `org.febit.boot.util`）：

- **错误码集中枚举**——禁止散落字符串 / `new RuntimeException("xxx not found")`；
  每类错误定义 `*Errors implements IErrors`（如 `AuthErrors`）。
- **枚举常量即契约三元组**——`(int status, ILogger logger, String code)`：
  status 是 HTTP 语义码，logger 是日志级别，code 是 i18n 消息键。
- **状态码带语义**——404 不存在 / 400 状态机非法 / 409 乐观锁冲突 / 500 系统错误。
- **文案外置**——code 经 `I18nUtils`（Spring `MessageSource`，`i18n/messages`）解析，
  未命中回退 code 本身；占位用 `{0}` `{1}`，不拼接。
- **断言式构造**——`Errors.NOT_FOUND.whenNull(e, "not found: {0}", id)` / `whenFalse(...)` /
  `whenFailed(response, ...)` / `response(msg)` / `exception(msg)`。

```java
@Getter @RequiredArgsConstructor
public enum XxxErrors implements IErrors {
    NOT_FOUND(404, Logger.DEBUG, "xxx.not-found"),
    INVALID_STATE(400, Logger.INFO, "xxx.invalid-state"),
    CONFLICT(409, Logger.DEBUG, "xxx.conflict"),
    ;
    private final int status;
    private final ILogger logger;
    private final String code;
}
```

## before / after

| 坏（默认改）                              | 好                                                     |
|-------------------------------------------|--------------------------------------------------------|
| `throw new RuntimeException("not found")` | `Errors.NOT_FOUND.whenNull(e, "not found: {0}", id)`   |
| `return "失败: " + reason`（硬编码文案）  | `XxxErrors.INVALID_STATE.response("invalid: {0}", id)` |
| 文案写死在枚举常量里                      | code 是 i18n 键，文案在 `i18n/messages*.properties`    |
| 错误码散落字符串常量                      | 集中 `*Errors` 枚举，枚举名即契约                      |
| 404 返回 200                              | 状态码带语义（404 / 400 / 409 / 500）                  |


