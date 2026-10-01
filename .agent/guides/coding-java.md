# 代码约定（Java）

> Java 代码的空安全、命名、风格、卫语句与禁止项；写 Java 代码时读。

相邻：[code-comment](code-comment.md) / [reliability](reliability.md) /

## 一、空安全（分层策略）

**逻辑代码包**（`component` / `handler` / `resolver` / `util` 等实现类所在包）：默认非空。

- 包以 `package-info.java` 声明 `@NullMarked`。
- 允许 `null` 的参数 / 可能返回 `null` 的方法显式 `@Nullable`；查询类方法（`findXxx` / `getXxx`）默认标 `@Nullable`。
- 未标注即非空。

**数据载体包**（DTO / VO / State / Event 等 POJO 所在包）：默认可空。

- `package-info` 不声明 `@NullMarked`。
- 字段 / 参数主动标 `@Nullable`。
- 仅恒定非空者（必选主键、`@Builder` 必选项、`of()` 必选参数）才标 `@lombok.NonNull`。

**注解来源**：静态分析用 `@Nullable` / `@NullMarked`（`org.jspecify.annotations.*`，
禁 `jakarta` / `javax`）；运行期非空校验用 `@lombok.NonNull`。

**`@NonNull` 与 `@Builder` 必须写完整名 `@lombok.X`，不 `import`**——二者高风险：

- `@lombok.NonNull`（运行期断言）易与 `org.jspecify.annotations.NonNull`（仅静态分析）、
  `jakarta.validation.constraints.NotNull`（Bean 校验）同名混淆。
- `lombok.Builder` 与 `@Builder` 生成的同名 `Builder` 内部类冲突。

其余首层注解（`Getter` / `Setter` / `RequiredArgsConstructor` / `AllArgsConstructor` / `With` 等）低风险，可 `import`。
嵌套注解 `lombok.extern.*`（`@Slf4j` / `@Jacksonized`）与 `lombok.experimental.*`
（`@Accessors` / `@SuperBuilder` / `@UtilityClass`）始终可 `import`。
详见 [lombok skill](../skills/lombok/SKILL.md)「关键红线」。

**禁用 `@Data`**——改 `@Getter` + `@Setter`，字段至少 5 个用 `@Builder`。
它隐含 `@ToString`，会把 token / 密钥等敏感字段拼入 `toString()`，一旦被日志或异常栈打印即泄漏密钥
（另两个隐含坑见 [lombok skill](../skills/lombok/SKILL.md)）。

## 二、命名

- 风格克制优雅：贴合 Java 生态主流（领域名 + 动作动词），拒绝复古、冗余、过度设计；自解释、无歧义缩写。
- 方法：动词 + 宾语，宾语不重复类名词根。`get(key)` / `find(name)` / `isReady()` / `run(ctx)` / `execute(task)`；
  不用 `getCachedValue` / `findByName` / `isStateReady` / `executeTask`。
- 动词用 `get` / `find` / `build` / `create`、`check` / `verify`、`handle` / `start` / `stop`；
  不用 `retrieve` / `instantiate` / `validate` / `commence`。
- 名字反推行为须一致（名字即契约）； **改名前先反推校验**、列出受影响调用点，待用户选定方案再落地。

## 三、风格

编码 / 换行 / 缩进 / 行宽由 `.editorconfig` 强制；import 分组与未用 import 清除由 Spotless
`importOrder` + `removeUnusedImports` 强制（`lombok` / `org.jspecify` 一组，`jakarta` / `java` / `javax` 一组）。

- 局部变量用 `var`；集合用 `List.of()` / `Map.of()`。
- Lombok 注解选型见 [lombok skill](../skills/lombok/SKILL.md)，Builder 深入见
  [builder.md](../skills/lombok/references/builder.md)。
- 空安全 / 集合 / 并发 / OOP 通用套路见 [clean-code](../skills/clean-code/SKILL.md)

### POJO 构造范式（工厂转换 vs 装配）

| 场景                      | 形态                                         |
|---------------------------|----------------------------------------------|
| 工厂转换（单源 → 值对象） | `static XxxVO of(...)`，逐字段 `setXxx` 装配 |
| 非转换的多字段装配 / 聚合 | `@Builder`，必选字段标 `@lombok.NonNull`     |
| 跨层散装                  | `new` + `setXxx()`                           |

意图优先于字段数量：转换用 `of()`，装配聚合用 `@Builder`。 **禁止 `@Data`**（理由见上文「空安全」一节）。

### 构造形态判据（惯例序 / builder / 静态工厂）

三档，上档只在下档失效时启用：

```
1. 惯例序位置传参（默认）   username, password / x, y, z / from, to
2. @Builder（惯例失效时）   序列不可猜 / 多可选 / 需默认值 / 需 copy-with
3. 类型上的静态工厂         需命名场景 / 兜底 / 不变量校验 / 返回类型收敛
```

- 位置传参 vs `@Builder` 的分界 **不在参数个数**，在 **序列是否已成习惯**（可猜性）；数字阈值只作提醒。
  判据、例外与失效条件见 [builder.md](../skills/lombok/references/builder.md)。
- **静态工厂建在被构造类型自身**（`X.of(...)`），不得旁挂 `XxxUtils` / `XxxFactory`——旁挂即消费方拥有构造 API。
  工厂须「改变至少一件事」；纯 pass-through（参数 → builder → build）不建。
- **引入机制的举证责任在抽象方**：主张加 builder / 工厂 / 包装者，须说出「不这么做会出什么具体错」。
- **反向举证同责**：判「机制多余 / 该删」须给出不依赖使用计数的损害；「当前无调用点」不构成证据——
  机制可为扩展预留、为形态统一而存在。删它的理由只能是「有具体害处」或「形态取向已变」。

## 四、卫语句

非平凡前置检查优先卫语句提前返回；多条件各自独立，不合并。

## 五、禁止项

- **魔法值**——用常量 / 枚举。
- **吞异常**——捕获必须记日志 / 重抛 / 转错误码三者之一；只 `log` 不带异常对象亦算吞。
- **`if` / `for` / `while` 无大括号**。
- **`switch` 漏分支**——穷尽 switch 表达式与 sealed 匹配由编译器兜底， **不得加 `default`**
  （加了会掩盖新增枚举 / sealed 子类时的编译错误）；仅传统 statement switch 需 `default`。
- **遍历 Map 取值用 `keySet()`**——要 value 用 `entrySet()`；只要键本身才用 `keySet()`。
- **可变出参**——`boolean[]` / `AtomicReference` → 语义化 record。
- **注释不合规**——复述 / 分段 / 裸 TODO，见 [code-comment](code-comment.md)。
- **集合初始化不指定大小**——如 `new HashMap<>()` 须给定初始容量。
- **`@Data`**——见上文「空安全」一节。
- **Web 入参字段用基本类型**——须用包装类型（`Boolean` / `Integer` / `Long`）：
  基本类型以默认值兜底，无法区分「客户端未传」与「显式传 `false` / `0`」；
  必选标 `@NotNull`、可选标 `@Nullable`，均不依赖默认值。 **出参 VO / DTO 不受此限**（值必定存在，如 `boolean admin`）。
- **裸 `ObjectMapper`**——必须走 `JacksonUtils` / `JacksonCodec`，
  否则丢失统一配置（命名策略、模块注册、null / 时区）。
- **Jackson V2 运行时包**——`com.fasterxml.jackson.databind` / `core` → 统一 V3 `tools.jackson.*`。
- **字符串 `equals` 反推枚举**——直接用枚举类型，勿以字符串比较驱动枚举映射。
- **参数过多**——单方法参数至少 6 个，或同一组参数在至少 4 处重复传递 → 抽语义化参数对象 / record。
- **多行文本用 `+` 拼接**——多行文本 / 内嵌 JSON/YAML 用 Text Block（`"""…"""`）。

例外（两条，勿「修正」）：

- `com.fasterxml.jackson.annotation.*`（`@JsonIgnore` / `@JsonPropertyOrder` / `@JsonCreator` 等）
  在 Jackson 3 中仍是该包名，不属 V2 违规。
- `springdoc/` 集成层（含 `springdoc/swagger/`）：swagger-core 的 ModelResolver 尚不兼容 Jackson 3，
  该层允许引用 Jackson V2 类型（见层内 `FIXME`）；其余代码统一 V3。
