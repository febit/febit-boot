# 函数设计范式（functions）

> 本文件给 **正向范式与语料**；红线声明见 SKILL.md 入口。
> 原则见 [AGENTS.md](../../../../AGENTS.md) 设计原则「测试友好」。
> 同类范式（边界收敛）见 [structure.md](structure.md)。

## 原则

- 做一件事，名即其事。
- 副作用明确、可预期；纯逻辑与 IO 分离（测试友好）。
- 命令查询分离（CQS）：要么改状态，要么返回，别混。

## 范式

### 卫语句提前返回，降嵌套

```java
boolean tryRenew(long now, long toleranceNanos) {
    if (renewTo <= now) return false;                  // Guard 1: already expired
    if (renewTo - now < toleranceNanos) return false;  // Guard 2: too close to fire
    this.at = this.renewTo;
    return true;
}
```

早返回优于深层 if-else；guard 配行内注释说明「为什么 return」（见 [comments.md](comments.md)）。

### 临界区收敛到单一语义方法

去抖器的 `process`：所有对 `Task` 的变更都包在 `cache.asMap().compute(key, ...)` 内——
把「按 key 串行化」的并发契约收敛到一个方法，调用方无需懂锁。

### 副作用在 `finally` 中确定发生

```java
try {
    action.run();
} finally {
    slot.lastNanos = clock.nanos();  // 即使抛异常也标记窗口
}
```

「即使抛异常也标记窗口」——副作用不依赖成功，契约稳定。
此不变量必须用命名 / Javadoc 暴露（见 [comments.md](comments.md)）。

### 不变量校验集中、启动即失败

`Configuration.validate()` 抛 `IllegalArgumentException`（`debounce<=0`、`maxDebounce<debounce`…）。
跨字段约束在一处校验，错误早暴露。

### 纯函数可独立测

`isThrottled(slot)` 无副作用、只读数——可单测。
把「是否节流」抽成纯函数，IO / 状态变更留给调用方。

### 私有工厂 / 默认实现隔离

`defaultThreadFactory` 私有静态；`NanosClock` 默认 `System::nanoTime`，测试可注入——
依赖可注入、副作用收敛。

## before / after

| 坏（默认改）         | 好                              |
|----------------------|---------------------------------|
| 深层 if-else 嵌套    | 卫语句早返回                    |
| 在多个方法里各自加锁 | 临界区收敛到 `compute()` 单方法 |
| 抛异常不标记窗口     | `finally` 中标记，契约稳定      |
| 跨字段校验散落多处   | `validate()` 集中，启动即失败   |


