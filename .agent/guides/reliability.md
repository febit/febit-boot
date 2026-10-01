# 可靠性设计指南

> 幂等性、错误处理与日志；涉及状态转换、异常与日志时读。
> 禁止项与 [coding-java](coding-java.md) 不重叠：本文件管状态写入的可靠性。

## 一、幂等性（原子状态转换）

所有状态变更通过乐观锁保证幂等：

```
1. get(key) → 读当前值 + 版本号
2. 前置检查（状态合法？）
3. 原子写入(key, newValue, expectedVersion) — 版本号不匹配则失败
4. 写入冲突 → 返回错误，由调用方决定重试或终止
```

### 错误处理

**核心规则**：包含状态写入、或被状态写入依赖的方法，不得吞掉错误。

```java
// 正确：每处失败都返回错误（前置检查 + 写入冲突）
IResponse<Void> complete(TaskId id, Report report) {
  var entry = store.get(id);
  if (entry.isEmpty()) return Errors.NOT_FOUND.response("not found: {0}", id);
  var state = entry.get();
  if (!state.canComplete()) return Errors.INVALID_STATE.response("invalid: {0}", id);
  var rev = store.update(entry.version(), state.complete(report));
  if (rev.isEmpty()) return Errors.CONFLICT.response("conflict: {0}", id);
  return IResponse.ok();
}

// 错误：失败静默（缺前置检查、缺冲突处理、假设结果非空）
void complete(TaskId id, Report report) {
  var state = store.get(id).get();       // 可能 NPE
  store.put(id, state.complete(report)); // 非 RUNNING 也覆盖，冲突静默丢失
}
```

前置检查必须覆盖 **存在性**与 **状态合法性**两类边界，不得假设输入合法：
空集合、缺行、非法状态都要显式返回，不是不可能事件。

乐观锁冲突返回 `Errors.CONFLICT`，是否重试由调用方决定（框架不自动重试）；
状态非法（规则违反）立即返回错误，不可重试。批量操作中部分失败即整体失败，
错误须原样向上传递，不吞、不降级为部分成功。

## 二、健壮性设计原则

> 健壮性两条判据：错在该响处响、炸在该炸处炸。

- **脆弱范式 silent → loud**——任何「失败却被静默吞掉」的写法都是脆弱范式，必须翻成显式失败：
  原子写入失败抛异常而非返回 `false` 不处理；分支缺省抛 `Unsupported` 而非默默走旧路径；
  解析失败显式报错而非回退默认值掩盖。沉默的失败会在离源头很远的地方以更难懂的方式爆发。
- **作用域收纳**——副作用与可变状态收纳在最小作用域：跨层 / 跨请求的共享可变状态、全局单例、
  静态可变字段都是爆炸半径源，能局部就局部；必须共享时显式收敛到单一权威
  （见 [clean-code / structure](../skills/clean-code/references/structure.md)），不散落多份。

## 三、禁止事项

- 状态写入失败静默处理
- 被写入依赖的方法吞掉错误
- 吞异常、裸 `Exception`、可变出参——定义见 [coding-java](coding-java.md)

## 四、日志规范

| 级别    | 用途             | 生产环境 |
|---------|------------------|:--------:|
| `error` | 需立即处理的错误 |    是    |
| `warn`  | 潜在问题         |    是    |
| `info`  | 关键业务步骤     |    是    |
| `debug` | 调试信息         |    否    |

- 用 Slf4j（`@Slf4j`）；`{}` 占位符，禁字符串拼接。
- 禁 `e.printStackTrace()`；禁打印敏感信息。
- 关键路径必须有日志。
- 消息语义化：动作 / 状态 + 原因（必要时）+ 承接动作（可选）+ 跟踪参数（若有）。
