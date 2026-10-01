---
name: clean-code
description: 写好代码的正向范式与方法论（Java 手艺）——覆盖注释/Javadoc、命名、函数设计、错误文案、类/模块边界、类型：何时写、怎么写、before/after 语料。写公共 API / 工具类 / 需要解释"为什么"或"跨边界契约"的代码时使用。红线（禁令、分层、语言风格硬约束）见各 guides（coding-java / code-comment / reliability），本 skill 单向链接、不复制。
---

# clean-code — 写好代码的正向范式

> 方法论 / 手艺，与 guide 的 **红线**互补：guide 定「禁止什么」，本 skill 定「怎样写得好」。
> 红线只在 guides 定义一次；本 skill 与各 references 不重复。

各主题的 **红线归属**（查禁令去此处，本 skill 给范式）：

| 主题                     | 红线权威                                        |
|--------------------------|-------------------------------------------------|
| 注释                     | [code-comment.md](../../guides/code-comment.md) |
| 命名 / 空安全 / 语言风格 | [coding-java.md](../../guides/coding-java.md)   |

## 与各 guide 的边界

- **红线 / 强制**——[coding-java](../../guides/coding-java.md) /
  [code-comment](../../guides/code-comment.md) /
  [reliability](../../guides/reliability.md)：禁令、空安全、幂等、语言风格硬约束（每次改动触发）。
- **正向范式 / 手艺**——本 skill：决策树、how-to、before/after 语料（写代码时深读）。

## 注释决策树（语言无关核心）

写注释前先判：

1. **命名 / 结构 / 类型能表达？** → 不写（命名即文档）。
2. **缺失会导致错误 / 安全漏洞？**（竞态、越权、数据损坏、时序、设计取舍的可观察后果）
   → 写「安全 / 正确性」注释。
3. **跨边界契约，API 消费者必须知道？**（错误码语义、非显而易见的返回值语义、临时方案 + issue）
   → 写「协议 / 契约」注释。
4. **其他（做了什么、步骤、职责、来源、用途、历史）** → 删。

> 两分档（详细 / 极简）判定见 [code-comment](../../guides/code-comment.md)。

## 参考

- [comments.md](references/comments.md)——注释范式：Javadoc 详细档结构（类级 / 方法级 / 字段级 +
  行内仅写 why）与 before/after 语料。
- [naming.md](references/naming.md)——命名范式：概念 + 实现后缀、动词 + 宾语、布尔方法名、
  单位 / 边界进名、类型参数契约。
- [functions.md](references/functions.md)——函数设计范式：卫语句、临界区收敛、`finally` 中确定发生副作用、
  不变量集中校验、纯函数可测。
- [errors.md](references/errors.md)——错误文案范式：错误码集中枚举（枚举名即契约）、状态码语义、位置占位。
- [structure.md](references/structure.md)——类 / 模块 / 包边界范式：模块 DAG（单向）、包边界、状态收敛判据。
- [types.md](references/types.md)——类型设计范式：sealed 穷尽匹配、`@NullMarked` 空安全边界。
