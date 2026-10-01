---
name: lombok
description: Lombok 使用范式（lombok.*）——getter/setter/构造器/不可变/非空/访问器/工具类约定、构造形态选型（惯例序位置传参 / Builder / 类型上的静态工厂），及与 record、Jackson、Spring 注入的协作。写/改 Java 实体、DTO、枚举、配置、值对象、工具类时读；禁止 @Data / @Value，慎用 @NonNull，不手写样板。Builder 深入见 references/builder.md。
---

# Lombok 使用范式

> 本仓约定：优先 **record**（原生不可变）+ `@Getter` / `@RequiredArgsConstructor` 显式注解；
> **禁止 `@Data`**、 **`@Value`**（由 record 取代）。
> Builder 深入见 [references/builder.md](references/builder.md)。

## 选型决策表

| 你想做什么                                 | 用                                                  |
|--------------------------------------------|-----------------------------------------------------|
| 不可变值对象 / 枚举常量数据                | `record`；枚举用 `@Getter @RequiredArgsConstructor` |
| 不可变 + JSON 序列化 / 反序列化            | `record` + `@Jacksonized @Builder`                  |
| 可变请求 / 响应模型（Spring 绑定）         | `@Getter @Setter`（隐式无参构造器）                 |
| Spring 构造注入                            | `final` 字段 + `@RequiredArgsConstructor`           |
| 不可变单字段 copy-with                     | record 组件 + `@With`                               |
| 纯静态工具类                               | `@UtilityClass`                                     |
| 工厂式构建 + 默认值 / 不变量校验           | `@Builder` + 私有全参构造器 / 工厂方法              |
| 构造序列已成习惯（`x, y, z` / `from, to`） | 位置传参，**不套 Builder**                          |

## 常用注解速查

| 注解                                  | 用法                                 | 实例                     |
|---------------------------------------|--------------------------------------|--------------------------|
| `@Getter` / `@Setter`                 | 字段 / 类级存取器                    | 枚举、可变 `Form` / `VO` |
| `@RequiredArgsConstructor`            | final 字段构造器                     | 注入、枚举常量持有       |
| `@AllArgsConstructor(access=PRIVATE)` | 全参构造器（常配 builder）           | 不可变 Options 类        |
| `@Accessors(fluent=true)`             | `x()` 而非 `getX()`                  | 链式 getter 的配置类     |
| `@With`                               | 不可变单字段 copy-with               | record 状态对象、stub    |
| `@NonNull`                            | 构造 / 方法 / builder 参数非空校验   | 不可变类的 final 字段    |
| `@Builder` + `@Jacksonized`           | 建造者 + Jackson 经 builder 反序列化 | record DTO / 事件 data   |
| `@Slf4j`                              | 注入 `log` 字段                      | 组件 / 处理器            |
| `@UtilityClass`                       | 纯静态工具类                         | `ResponseUtils`          |

> 以下注解 **不该主动引入**：`@Data`、`@Value`、`@SneakyThrows`、`@FieldDefaults`、
> `@NoArgsConstructor`、`@EqualsAndHashCode`、`@ToString`（`@Data` 另被红线禁用）。

## 范式 1：不可变枚举

枚举常量数据用 `record` 式不可变：`@Getter @RequiredArgsConstructor` + `private final`。

完整代码见 [patterns.md](references/patterns.md)。

## 范式 2：可变 DTO / VO（Form / VO）

> `@Getter @Setter` 生成隐式无参构造器 + setter，Spring / Jackson 经 setter 绑定请求体。
> **不要用 `@Data`**：字段标 `@NonNull` 时会破坏无参构造器。

完整代码见 [patterns.md](references/patterns.md)。

## 范式 3：不可变配置（私有构造器 + 工厂）

> 公开 fluent getter + 私有全参构造器 + 私有 builder → 外部只能经工厂方法构造成立的对象
> （详见 [builder.md](references/builder.md)）。

完整代码见 [patterns.md](references/patterns.md)。

## 范式 4：纯静态工具类

> `@UtilityClass` 自动：构造器私有化、`final` 类、方法转 `static`。
> **不要**叠加 `@Component` / `@Service`（无实例，无法注入）。

完整代码见 [patterns.md](references/patterns.md)。

## 范式 5：不可变状态记录

`record` 组件 + `@With` 做单字段派生，返回新实例，不暴露可变 setter。
与 [builder.md](references/builder.md) 的 `toBuilder` 互补——单字段改 `@With`，多字段改 `toBuilder`：

> `@With` 生成 `withFallback(n)` / `withLastError(err)` 等；调用方 `var next = state.withLastError(err)`
> 拿新实例，原 `state` 不变。状态机 / 快照字段（含 `null`）用 `@Nullable` 标注。

完整代码见 [patterns.md](references/patterns.md)。

## 关键红线

- **禁止 `@Data`**——红线与 `@ToString` 泄密理由见 [coding-java](../../../guides/coding-java.md)；
  此处补另外两坑：`@EqualsAndHashCode`（全字段 equals）、`@RequiredArgsConstructor`（吞无参构造器，
  破坏 `Models.map(entity, VO::new)` / 分步 setter 装配）。
  改用显式 `@Getter` / `@Setter` / `@Builder` / `record`；确需 `toString` 时手写并 `@ToString.Exclude` 敏感字段。
- **优先 record + 显式注解，不用 `@Data` / `@Value`**——record 自带不可变 + 组件 getter，
  配合 `@Jacksonized` 即可 JSON。
- **`@Setter` 仅用于可变 DTO（Form / VO）**——领域实体 / 值对象保持不可变（record / `@Getter`）。
- **`@UtilityClass` 不要叠 `@Component` / `@Service`**——工具类无实例，Spring 无法注入；
  调用方用 `@RequiredArgsConstructor` 注入即可。
- **`@NonNull` 不要用于可变 VO 字段**——同 `@Data` + `@RequiredArgsConstructor` 陷阱：
  字段标 `@NonNull` 会被收进构造器参数，吞无参构造器，破坏 `Models.map(entity, VO::new)`。
  其余场景（构造器参数、builder 字段）`@NonNull` 是推荐的早失败手段。
- **`@Builder` 必须配 `@Jacksonized` 做 JSON 反序列化**——否则 Jackson 不进 builder，字段全 null。
- **`@RequiredArgsConstructor` 承载 Spring 注入**——`@Component` + `final` 字段 + 该注解 = 构造注入，
  不再手写 `@Autowired`。
- **枚举用 `@Getter @RequiredArgsConstructor` + 自定义构造器**——不在枚举上叠 `@Data`；常量名即契约。
- **`@NonNull` / `@Builder` 必须写完整名 `@lombok.X`，禁止 import**——二者高风险：
  `@lombok.NonNull`（运行期断言）易与 `org.jspecify.annotations.NonNull`（仅静态分析）、
  `jakarta.validation.constraints.NotNull`（Bean 校验）同名混淆；
  `lombok.Builder` 会与 `@Builder(builderClassName = "Builder")` 生成的同名 `Builder` 内部类冲突。
  其余首层注解（`Getter` / `Setter` / `RequiredArgsConstructor` / `AllArgsConstructor` / `With` 等）
  低风险，可 `import lombok.X;`；嵌套注解 `lombok.extern.*`（`@Slf4j` / `@Jacksonized`）与
  `lombok.experimental.*`（`@Accessors` / `@SuperBuilder` / `@UtilityClass`）始终可 `import`。
- **构造形态先看惯例，再谈机制**——位置传参 vs `@Builder` 的分界在序列是否已成习惯（非参数个数）；
  静态工厂只建在被构造类型自身（`X.of(...)`），旁挂 `XxxUtils` 即违规。
  判据 / 例外 / 失效条件见 [builder.md](references/builder.md)。

## 参考

- [patterns.md](references/patterns.md) —— 各「范式 N」的完整代码（枚举 / DTO / 配置 / 工具类 / 状态记录）。
- [builder.md](references/builder.md) —— `@Builder` / `@Jacksonized` / `@Builder.Default` /
  私有 builder / 工厂 builder（`@Nullable` + `nvl` 默认值）/ `@SuperBuilder` / `toBuilder`
