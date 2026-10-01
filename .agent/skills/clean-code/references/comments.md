# 注释范式（comments）

> 本文件给 **范式与真实语料**；红线声明见 SKILL.md 入口，不在此复述。
> 原则：命名即文档，注释是例外；只写「安全 / 正确性关键约束」与「协议 / 契约 + workaround」两类。
> 两分档判定（详细 / 极简）见 [code-comment](../../../guides/code-comment.md)；本文范式只用于详细档。

## Java — Javadoc 详细档范式

### 类级 Javadoc = 概念 + 心智模型 + 设计取舍 + 性质边界

```java
/**
 * A per-key debouncer backed by Caffeine cache + DelayQueue.
 *
 * <p>Conceptually, each key has a rolling delay window — every call resets the
 * timer, and the action fires only after the window expires without further calls.
 * ...
 * <p>It is safe for concurrent use: cache.asMap().compute() serializes callers
 * of the same key.  This is in contrast to a plain DelayQueue (which lacks
 * per-key deduplication) or a global debouncer (which cannot track multiple keys).
 * <p>This is a best-effort local debouncer, not a distributed lock —
 * cache eviction triggers early pipeline...
 *
 * @param <K> key type with correct equals/hashCode
 */
```

要素：

1. **一句话概念**：`<Class> backed by <X>` / `<Class> for <purpose>`。
2. **心智模型**：用 `<p>` 讲「它是什么」的通俗模型（rolling delay window），不复述实现。
3. **与替代方案对比**：「in contrast to a plain DelayQueue / global debouncer」——解释为何这样设计。
4. **性质边界**：明确「是什么 / 不是什么」——「best-effort local, not a distributed lock」
   （删此句会导致误用为分布式锁 → 属安全 / 正确性注释）。
5. **类型参数契约**：`@param <K>` 写对 `equals` / `hashCode` 的要求。

### 方法级 Javadoc = 非显而易见的契约 + 返回值语义 + 陷阱

```java
/**
 * Run action if this key is not throttled, otherwise skip.
 * The window is marked even if the action throws — retries within the window are skipped.
 *
 * @return true if the action ran, false if skipped
 */
```

要素：

- 写 **签名表达不了的契约**：如「即使抛异常也标记窗口」——调用方极易误判的行为。
- `@return` 给 **无歧义语义**：「true if ran, false if skipped」。
- **陷阱用 Warning 显式标注**：如「throttled 与 supplier 返回 null 都产生 empty Optional」——
  调用方用 `isEmpty()` 检测节流会误判，属误用风险，详细档必写。
- `@param` / `@return` / `@throws` 仅在签名表达不了关键约束时保留。

### 字段 Javadoc = 语义 + 约束 + 典型值 + 副作用

- `debounce`：`Must be positive. Typical values: 100–500 ms for UI-style coalescing, 1–5 s for config reload.`
- `maxDebounce`：`Without this, a continuously refreshed key never fires.`
  ——解释字段的 **存在动机**（非显而易见的设计理由）。
- `tolerance`：分 **Purpose**（为何存在）+ **Side effect**（实际触发可能早于 `tolerance`）——
  把权衡写清，调用方不被 surprises。

要素：每个可选 / 必填字段写「语义 + 约束 + 典型值 + 副作用」。

### 行内注释：仅「为什么 / 正确性」

```java
if (task != target) {
    // target is stale (replaced by a newer task in cache), ignore it.
    return task;
}
// Guard 1: already expired
// Guard 2: too close to fire, skip renew
// Reuse task, overwrite action, update renew time
```

- 只解释 **为什么这样分支 / 正确性**（stale 任务必须忽略、guard 的意图），不复述「做了什么」。
- 复杂 `compute()` lambda 内用简短行内注释标注分支意图是可接受的（非显而易见的控制流）。
- 禁止：`// 计算总分`、`// 避免 N+1`、`// 由 Xxx 注入` 这类复述 / 来源 / 用途。

### 范式总结（「必要注释」六条）

1. **类级给心智模型**，不是功能列表：概念句 + 通俗模型 + 替代方案对比。
2. **明确定义性质边界**：「是 X，不是 Y」（local not distributed / best-effort not guarantee）——防误用。
3. **写非显而易见的契约**：即使异常也标记、返回值无歧义语义、并发安全性。
4. **陷阱用 Warning 显式标注**：易踩的返回歧义 / 边界条件。
5. **字段写动机与副作用**：尤其可选字段「为何存在」「设错会怎样」。
6. **行内注释只写 why**：正确性 / 分支意图，不复述 what。

> 这套范式 **只在详细档（公共 API / 工具类）使用**；内部代码走极简档。

## before / after

| 坏（默认删）                          | 好（详细档必要）                                           |
|---------------------------------------|------------------------------------------------------------|
| `// 计算总分`（复述）                 | `// 总分可能溢出 int，用 long`（正确性）                   |
| `// 由 store 注入`（来源，可追踪）    | `// 最后 Owner 保护：不得移除`（安全）                     |
| `// 避免逐行 N+1`（动机，名字已表达） | `// 防 lost-wakeup：先注册通道再查库`（正确性）            |
| `// 供 Xxx 使用`（用途）              | `// best-effort local, not a distributed lock`（性质边界） |
| 无 Javadoc 的公共工具类               | 类级心智模型 + 方法选择指南 + 返回值语义                   |
