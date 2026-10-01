# Lombok Builder — 深入用法

> 用法与约定；红线与速查见 [SKILL.md](../SKILL.md)。
> 包：`lombok.Builder` / `lombok.extern.jackson.Jacksonized` / `lombok.experimental.Accessors`。

## 0. Builder 还是构造器？（选型）

| 场景                               | 选                                                                      |
|------------------------------------|-------------------------------------------------------------------------|
| 不可变值对象、字段多、需 JSON 往返 | `record` + `@Jacksonized @Builder`                                      |
| 不可变、字段少、构造即完整         | `record`（组件构造器）或 `@RequiredArgsConstructor`                     |
| 可变请求 / 响应模型（Spring 绑定） | 不用 Builder：`@Getter @Setter`                                         |
| 需默认值 + 不变量校验的不可变对象  | `@Builder` + 私有全参构造器 / 工厂方法（见「私有 builder + 工厂方法」） |
| 基于已有实例改若干字段             | `toBuilder=true`（见「`toBuilder`」）或 `@With`                         |

经验法则：反序列化进来的对象用 `@Jacksonized @Builder` 或 `@Getter @Setter`；
纯粹内部构建、无需 JSON 的，优先 record / 构造器，别硬套 Builder。

三档判据、例外清单与失效条件见「判据：惯例序 / builder / 静态工厂」。

## 1. 基础：`@Builder` + `@Jacksonized`（DTO / 事件 data）

通用形态——record + `@Jacksonized` + `@lombok.Builder`，既给建造者又支持 Jackson 经 builder 反序列化：

```java
@Jacksonized
@lombok.Builder(builderClassName = "Builder")
public record XxxEventData(String from, String to) {
    public static class Builder { /* 可顺带实现自定义 builder 接口 */ }
}
```

> `builderClassName = "Builder"` → 生成 `XxxEventData.builder()`，无需手写构造器。

## 2. `@Builder.Default` — builder 默认值

```java
@Jacksonized
@lombok.Builder
public record XxxRequestForm(
        String ref,
        @lombok.Builder.Default int concurrency = 1
) {}
```

> 不加 `@Builder.Default` 时，字段初始化值对 builder 无效（生成 `concurrency = 0`）。

## 3. 私有 builder + 工厂方法

公开 fluent getter，但构造只走工厂——防止外部拼出不变量未校验的对象：

```java

@Accessors(fluent = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Builder(
    builderClassName = "InternalBuilder",
    builderMethodName = "internalBuilder",
    access = AccessLevel.PRIVATE)
public record XxxOptions(@lombok.NonNull String name) {
  // …其他 @NonNull final 字段

  @lombok.Builder(builderClassName = "Builder")   // 公开的「创建」入口
  private static XxxOptions create(
      @lombok.NonNull String name,
      @lombok.NonNull ConnectionFactory cf,
      @org.jspecify.annotations.Nullable Clock clock,
      // …其余 @Nullable 可选参数
  ) {
    clock = org.febit.lang.util.Defaults.nvl(clock, Clock::systemUTC);  // 默认值
    if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
    return XxxOptions.internalBuilder()
        .name(name).connectionFactory(cf).clock(clock) /* … */.build();
  }
}
```

> 两个 builder：私有的 `InternalBuilder`（全字段，含 `@NonNull` 校验）+
> 公开的 `Builder`（工厂 `create` 的参数集，可选参数用 `@Nullable` + `nvl` 兜底）。
> 外部 `XxxOptions.builder().name(...).connectionFactory(...).build()` 自动套用默认值与校验。

## 4. `@Builder` 在类（非 record）上的要点

```java
@lombok.Getter
@lombok.Builder
public class XxxVO {
    private final String id;
    @lombok.NonNull private final String name;
}
```

- `@NonNull` 字段 → builder 的 `name(...)` 自动非空校验。
- 类用 `@Builder` 时 **不再生成全参构造器**；若还需全参构造器（如 ORM / 序列化）叠加 `@AllArgsConstructor`。

## 5. `toBuilder` — 基于已有实例派生

```java
@lombok.Builder(toBuilder = true)
public record XxxState(...) { ... }

var next = current.toBuilder().status(RUNNING).build();   // 拷贝后改字段
```

> 状态机 / 不可变 copy-with 常用；与 `@With` 互补（`@With` 改单字段、`toBuilder` 改多字段更顺）。

## 6. `@SuperBuilder` — 继承链上的 builder

```java
@lombok.experimental.SuperBuilder
public abstract class BaseEvent { ... }
@lombok.experimental.SuperBuilder
public class XxxEvent extends BaseEvent { ... }
```

> 仅当存在类继承且子类 builder 要包含父类字段时用。`record` 无继承，优先组合 / record。

## 红线速记

- **DTO / 事件 data 一律 `@Jacksonized` + `@Builder`**——否则 Jackson 反序列化不进 builder，字段全 null。
- **默认值必须 `@Builder.Default`**——裸初始化值对 builder 不生效。
- **需全参构造器的类叠 `@AllArgsConstructor`**——`@Builder` 不生成全参构造器。
- **不变量校验放工厂 / `@NonNull`**——builder 入口应产出合法对象，不上抛半成品。
- **避免 `@SuperBuilder`**——继承场景优先 record / 组合。
- **`@Builder` 与 `@NoArgsConstructor` 冲突**——二者直加会编译报错；要并存须补 `@AllArgsConstructor`。
  不写 `@NoArgsConstructor`（record + `@Jacksonized` 优先，可变 DTO 用 `@Getter @Setter`）。

## 7. 判据：惯例序 / builder / 静态工厂

位置传参、`@Builder`、静态工厂 **不是三选**，而是三档，上档只在下档失效时启用。
三档总览见 [coding-java](../../../../guides/coding-java.md)；本文展开各档的例外与失效条件。

**1. 惯例序（默认档）**——分界不在参数个数，在序列是否已成习惯（可猜）。满足其一即可位置传参：

| 信号              | 例                                                                    |
|-------------------|-----------------------------------------------------------------------|
| 生态 / 标准库既存 | `compare(a, b)`、`Point(x, y)`、`BasicAuth(u, p)`、`copyOf(src, dst)` |
| 顺序颠倒即荒谬    | `password, username`、`y, x`、`to, from`                              |
| 元组本身有名字    | 坐标 / 区间 / 尺寸 / 时长 / 凭据对                                    |
| 领域固定搭配      | `hour, minute, second`、`r, g, b`、`min, max`、`width, height`        |

**2. `@Builder`（惯例假设失效时）**：

| 失效形态                         | 例                                                                |
|----------------------------------|-------------------------------------------------------------------|
| 超惯例长度                       | `x, y, z` + `time`；`username, password` + 第三参                 |
| 惯例外参数混入                   | 惯例对后追加 `rememberMe` / `captcha` / 任意可选参                |
| 同类型且无惯例                   | `token` / `secret` / `id` / `path` 任意排列——传错不荒谬，必须具名 |
| 多可选 / 需默认值 / 需 copy-with | `@Builder.Default` / `toBuilder`                                  |

**3. 静态工厂**——判据是「改变了什么」，不是「减少了什么」：至少改变一件事
（默认值 / 归一 `nvl`、不变量校验、返回类型收敛、前置动作、命名场景），
且必须建在被构造类型自身；纯 pass-through（参数 → builder → build）恒为负收益。
把字段装配塞进工厂 = 把显式传错换成隐式传错。

> 并存范式： **私有 builder（全字段）+ 公开工厂（少参、带默认值与校验）**。

**反例**：把惯例名与惯例序一起包掉——`basicAuth(username, password)`
被包成 `login(baseUrl, username, password)`。

**自检三问**：删除测试（删掉它谁受损？）/ 内联测试（内联后可读性是否下降？）/
反事实（不抽会出什么具体损害？）——皆「无」即不建。

> 三问的答案不得取自「当前调用点数量」；判据是可猜性与具体损害。
