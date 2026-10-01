# 类 / 模块边界范式（structure）

> 本文件给 **边界判定的正向范式与决策**——「它该属哪个模块 / 哪个包 / 哪个类」；
> 红线声明见 SKILL.md 入口。
> 同类范式（函数内临界区收敛）见 [functions.md](functions.md)。

## 原则

边界清晰、无越级、无反向依赖。读来「理所应当」——职责归属合直觉，调用顺滑，
无需注释解释「为何在此」。若迟疑「它该属哪层」，即越界信号。

## 模块边界（Gradle 多模块）

本仓模块图（依赖单向，下层不得依赖上层）：

```
febit-boot-bom / -dependencies → 仅依赖管理（BOM），不产出代码
febit-boot（core，目录 boot）  → 不依赖任何 starter；仅 febit-lang + 编译期 API
febit-boot-starter-webmvc      → api project(':febit-boot')
febit-boot-starter-jwt         → 独立 starter
febit-boot-starter-jooq        → 独立 starter
```

| 层                   | 允许                                | 禁止（越界信号）                             |
|----------------------|-------------------------------------|----------------------------------------------|
| core（`febit-boot`） | 定义契约、自动配置骨架、工具类      | 反向依赖 starter；把可选集成实现编进 core    |
| starter              | 聚合可选依赖（`api`）+ 自动配置入口 | 反向依赖另一个 starter；承载 core 的领域逻辑 |

- **可选依赖隔离**：core 对 servlet / spring-web / jOOQ / okhttp / springdoc 等集成相关类型用
  `compileOnly`（运行时由消费方 starter 提供），避免把可选依赖传染给所有使用者；starter 才用 `api` 声明。
- **跨模块协作只经公开契约 + 共享模型**，不外泄内部工具类；禁止反向 / 成环。

## 包边界（core 内）

`org.febit.boot.<feature>` 一功能一包（`auth` / `permission` / `web` / `springdoc` /
`validation` / `model` / `module` / `util`）：

- 功能包自带 `package-info.java` 声明 `@NullMarked`
  （空安全分层见 [coding-java](../../../guides/coding-java.md)）。
- 通用工具收敛到 `util`，不散落各功能包。
- 自动配置类置于根包（`@Configuration(proxyBeanMethods = false)` + `AutoConfiguration.imports` 注册），
  只做装配、不承载领域逻辑。

范式（边界清晰）：

- **临界区收敛单方法**：把易错的不变量（如按 key 串行化）收进一个方法，调用方无需懂锁。
- **依赖可注入**：时间源 / 工厂用 `@FunctionalInterface` 提供默认实现，测试可注入。
- **共享可变状态收敛到单一权威**：全局单例 / 静态可变字段是爆炸半径源，能局部就局部；
  必须共享时收敛到一处，不散落多份。

## before / after

| 坏（越界信号）                                | 好                                      |
|-----------------------------------------------|-----------------------------------------|
| core 反向依赖某 starter                       | starter → core 单向                     |
| core 把 servlet / jOOQ 等可选依赖声明为 `api` | core 用 `compileOnly`，starter 用 `api` |
| 通用工具散落各功能包                          | 收敛到 `util`                           |
| 自动配置类里写领域逻辑                        | 自动配置只做装配，逻辑归功能包          |
| 全局静态可变字段散落                          | 收敛到单一权威或改局部                  |


