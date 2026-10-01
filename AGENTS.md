# AGENTS.md

> 本文件是 **唯一常驻入口**：只放设计原则、规范文档索引、任务路由与红线自检；细节一律外链。
> 改完自检链接；每份文档须从本文件经链接可达。

## 设计原则

编码、命名、选型、分层时主动贯彻；每条只给要旨，展开见指针。

- **命名**：自解释、语义精确；贴合语言生态、克制优雅；同一概念全仓用词统一。
  展开：[coding-java](.agent/guides/coding-java.md) / [clean-code / naming](.agent/skills/clean-code/references/naming.md)
- **规范**：最佳实践为底线、对标业内标杆；不重复造轮子、不退化为「能跑就行」。
  展开：[clean-code](.agent/skills/clean-code/SKILL.md)
- **API 设计**：贴合 Spring Boot / Java 生态惯例（starter 依赖边界、`AutoConfiguration.imports` 注册、`IResponse`
  统一返回）；关键选型先调研同类实现再定。
- **分层**：同层范式一致、边界不越界、归属读来理所当然。
  展开：[clean-code / structure](.agent/skills/clean-code/references/structure.md)
- **测试**：依赖可注入、副作用收敛、纯逻辑与 IO 分离；关键契约有测试守护。
  展开：[testing](.agent/guides/testing.md)
- **健壮**：错在该响处响、炸该炸处炸（不静默、不掩盖）。
  展开：[reliability](.agent/guides/reliability.md)

---

## 规范文档

| 优先级 | 文档                                            | 覆盖                                 |
|:------:|-------------------------------------------------|--------------------------------------|
|   P0   | [coding-java](.agent/guides/coding-java.md)     | 空安全 / 命名 / 风格 / 卫语句 / 禁令 |
|   P0   | [reliability](.agent/guides/reliability.md)     | 幂等 CAS / 错误处理 / 日志           |
|   P1   | [code-comment](.agent/guides/code-comment.md)   | 注释：原则 / 两类 / 三禁令 / 两分档  |
|   P1   | [testing](.agent/guides/testing.md)             | 测试继承体系 / 组织 / 命名           |
|   P1   | [documentation](.agent/guides/documentation.md) | 文档只记稳定之物                     |

> 本表只登记存在性与优先级，供查阅；加载以「按需加载」命中行为准。

### 文档模板

写 `doc/**/*.md` 前在此取模板；完整索引见 [templates/README.md](.agent/templates/README.md)。

| 我要做…                   | 模板                                            |
|---------------------------|-------------------------------------------------|
| 写 / 重构目录 README 导航 | [doc/index.md](.agent/templates/doc/index.md)   |
| 写设计文档                | [doc/design.md](.agent/templates/doc/design.md) |

## 按需加载

判定流程： **先判任务类型 → 命中下表一行 → 只读该行「必读」列**；其余行、其余列一律不读。
「按需」列只在实际写到对应工具 / 框架时才展开， **绝不预读**。

| 任务                   | 必读                                                                                      | 按需                                                                           |
|------------------------|-------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| 写 / 改 Java 代码      | [coding-java](.agent/guides/coding-java.md) / [reliability](.agent/guides/reliability.md) | [lombok](.agent/skills/lombok/SKILL.md)                                        |
| 写注释 / Javadoc       | [code-comment](.agent/guides/code-comment.md)                                             | [clean-code](.agent/skills/clean-code/SKILL.md)                                |
| 写好代码正向范式       | —                                                                                         | [clean-code](.agent/skills/clean-code/SKILL.md)                                |
| 用 febit 工具库 / JSON | [coding-java](.agent/guides/coding-java.md)                                               | —                                                                              |
| 写测试                 | [testing](.agent/guides/testing.md)                                                       | —                                                                              |
| 写 / 改 `doc/**/*.md`  | [documentation](.agent/guides/documentation.md)                                           | —                                                                              |
| 改 skill / guide       | —                                                                                         | [skill 规范](.agent/skills/authoring.md) / [指南规范](.agent/guides/README.md) |

> 目录全貌见 [.agent/README.md](.agent/README.md)；skill 清单见 [skills/README.md](.agent/skills/README.md)。

---

## 快速自检

每次改动后对照，按危害从重到轻。 **完整规则以指南为唯一权威**，此处只列红线名与出处。

### 数据与状态

| 红线                                                                       | 出处                                                                                      |
|----------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| 状态写入失败静默处理（silent → loud）                                      | [reliability](.agent/guides/reliability.md)                                               |
| 包含 / 被依赖状态写入的方法吞掉错误                                        | [reliability](.agent/guides/reliability.md)                                               |
| 原子状态转换不走乐观锁（版本 CAS + 冲突返回 `CONFLICT`）                   | [reliability](.agent/guides/reliability.md)                                               |
| 敏感字段（token / 密钥）进 `toString()` / 日志（`@Data` 隐含 `@ToString`） | [coding-java](.agent/guides/coding-java.md) / [reliability](.agent/guides/reliability.md) |

### 正确性

| 红线                                                                      | 出处                                        |
|---------------------------------------------------------------------------|---------------------------------------------|
| 吞异常（捕获不记日志 / 不重抛 / 不转错误码；只 `log` 不带异常对象也算吞） | [coding-java](.agent/guides/coding-java.md) |
| Web 入参字段用基本类型（无法区分「未传」与 `false` / `0`）                | [coding-java](.agent/guides/coding-java.md) |
| 穷尽 switch / sealed 加 `default`（掩盖新增枚举的编译错误）               | [coding-java](.agent/guides/coding-java.md) |
| 前置检查不覆盖存在性 + 状态合法性（假设输入合法）                         | [reliability](.agent/guides/reliability.md) |

### 一致性

| 红线                                                       | 出处                                            |
|------------------------------------------------------------|-------------------------------------------------|
| 裸 `ObjectMapper` / Jackson V2 未迁 V3（丢失统一配置）     | [coding-java](.agent/guides/coding-java.md)     |
| 构造形态错位（惯例序套 builder / 旁挂工厂 / pass-through） | [coding-java](.agent/guides/coding-java.md)     |
| 注释不合规（复述 / 裸 TODO / 解释他者）                    | [code-comment](.agent/guides/code-comment.md)   |
| 文档写实现细节 / 进度 / 妥协 / 同概念多处定义              | [documentation](.agent/guides/documentation.md) |
