# 命名范式（naming）

> 本文件给 **正向范式与语料**；红线声明见 SKILL.md 入口。
> 最高原则见 [AGENTS.md](../../../../AGENTS.md) 设计原则「命名即文档」。

## 最高原则：命名即文档

读标识符能推出 **职责 / 不变量 / 何时用** → 不写注释。
推不出 → **改名**，不用注释补（注释是失败补偿，见 [comments.md](comments.md)）。

## 决策

- 名字能否让调用方猜出：做什么 / 返回什么 / 关键约束？不能 → 改名。
- 同一概念全仓一个词（如 `debounce` / `maxDebounce` / `tolerance` 在 Debouncer 与 Throttle 间语义一致）。

## 范式

### 类 / 类型名 = 概念 + 实现后缀

- `CaffeineDebouncer` = 实现（Caffeine）+ 领域概念（Debouncer）：表明 backed by Caffeine。
- `Task` / `Slot` / `Configuration` / `Builder` / `NanosClock`：内部类型名精确表达角色，无歧义前缀。
- `NanosClock` 用 `@FunctionalInterface` 封装时间源（测试可注入）——名字即契约。

### 方法名 = 动词 + 宾语；布尔方法名不自藏语义

- `call` / `cancel` / `invalidateAll` / `close` / `touch` / `reset`：动词精确。
- 返回布尔：`execute` 返回 `true`=ran / `false`=skipped——名字不藏 `skipped`，
  靠 Javadoc `@return` 补契约（见 [comments.md](comments.md)）。
  `tryRenew` 的 `try` 前缀 = 可能失败 / 部分成功，语义自含。
- `estimatedSize`：形容词 + size，表明「近似」——名字即不变量（不承诺精确）。

### 字段名 = 精确无歧义，单位 / 边界进名

- `debounce` / `maxDebounce` / `tolerance` / `idle` / `maxSize`：每个词自解释。
- `windowNanos` / `lastNanos`：后缀显式单位，避免与 ms 混用（Code Complete：单位进名）。
- `at` / `renewTo` / `maxAt`：时间点字段用一致后缀 `At`，表达「时刻」。

### 类型参数

- `<K>` + `@param <K> key type with correct equals/hashCode`：把类型参数契约写进 Javadoc。

## before / after

| 坏（默认改）                      | 好                                                     |
|-----------------------------------|--------------------------------------------------------|
| `computeDelay` 含「计算」动词冗余 | 直接用语义名，动词留给方法                             |
| `tmp` / `data` / `obj` 泛名       | 具体语义名（调用方一眼懂）                             |
| `getVal` 返回布尔                 | 谓词 `isX` / `hasX` / `canX`；返回实体用 `getX` 或名词 |
| `dur` 缩写歧义                    | 单位进名：`windowNanos`                                |
| `refresh` 泛动作                  | `refreshConfig` / `reloadConfig`（动作 + 对象）        |

