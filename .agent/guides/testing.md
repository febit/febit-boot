# 测试实践

> 测试继承体系、组织方式、命名与断言；写测试时读。
> 写测试时读；本文件定义继承体系、组织、命名、断言与红线。

## 原则

- **遇到疑似 Bug，先指出再测试**——标注 `// FIXME`，不让测试拟合错误行为：
  拟合了错误行为的测试比没有测试更糟，它把缺陷固化成契约。
- **可读性优先于规范**——规范僵化损害可读时（如 `@Nested` 强拆反而增加层级），指出冲突并选可读性。

## 测试类型

| 类型                  | 继承                                     | 特点                                        |
|-----------------------|------------------------------------------|---------------------------------------------|
| 集成测试（Web / MVC） | `BaseMvcTest`（或同形态 `Base*MvcTest`） | `@SpringBootTest` + `@AutoConfigureMockMvc` |
| 单元测试              | 不继承                                   | 纯逻辑，不依赖容器                          |

## 表驱动测试（TableTest）

同一段断言逻辑被多组输入 / 期望复用时，写成对齐的数据表（表驱动），而非复制近邻测试。

- 适用：用例仅输入 / 期望不同、断言相同（等价类、边界值、错误码映射、状态转换矩阵）。
- 不适用：用例间 setup 或断言不同（拆独立测试或 `@Nested`）；用例 ≤ 2（表格收益不抵对齐成本）；
  需复杂前置 / 副作用的场景。

形式：`@TableTest`（来自 `org.tabletest.junit`）——管道分隔的表文本，首行表头、其余每行一个用例，值自动转为参数类型；
表由 `tableTestFormatter`（Spotless）对齐维护。首列可作场景名（仅展示，无对应参数）。

```java
// 正确：多组等价类落在一张表里
@TableTest("""
    input | valid
    "1+1" | true
    ""    | false
    "x"   | false
    """)
void parse(String input, boolean valid) {
  assertThat(Parser.parse(input).valid()).isEqualTo(valid);
}
```

// 错误：用例 ≤ 2 也拉表，或断言各不相同时硬塞一张表

## 组织

测试包镜像源码包：`src/test/java/...` 与 `src/main/java/...` 同包路径。

`@Nested` 按功能点分组， **只在主题边界清晰时使用**（场景至少 3 个且有明显分组价值）。
收益：方法名不需重复前缀、主题内可复用、跨主题风格一致。简单场景无需强行嵌套。

```java
class PermissionManagerTest extends BaseMvcTest {
  @Nested
  class Resolve { ...
  }   // 场景够多时才拆

  @Nested
  class Verify { ...
  }
}
```

`@Nested` 类名 **仅在与同包可见类型冲突时才追加 `_` 后缀**。

## 命名

简洁描述行为，不用 `test` 前缀：

```java
void basic() { ...}

void returns404WhenNotFound() { ...}
```

## 集成测试

继承 `BaseMvcTest`（MVC 端点）或按同形态自建 `Base*MvcTest`：
`@SpringBootTest(classes = ...TestApplication.class)` + `@ActiveProfiles("unit-test")` + `@AutoConfigureMockMvc`。

- `@Autowired` 注入被测对象；`@MockitoBean` 隔离外部依赖（如出站客户端）。
- API 测试优先 `MockMvc`：`mockMvc.perform(get(...)).andExpect(status()...)`。
- mock / stub 的响应体 JSON 勿用 `+` 拼接——用 Text Block 或多行常量，避免转义错误与长度失真。
- 断言：单元用 AssertJ `assertThat(...)`（JUnit `Assertions` 亦可）；
  响应断言用 MockMvc matchers（`status()` / `jsonPath()`，底层 Hamcrest）。

Record / DTO / VO 的测试应覆盖：构造默认值、`of()` 工厂方法、序列化 / 反序列化。

---

## 项目附加条款

| 项目    | 值                                                                                    |
|---------|---------------------------------------------------------------------------------------|
| 基类    | `BaseMvcTest` / `BaseAuthMvcTest`（`boot/src/test/java/org/febit/boot/web/mockmvc/`） |
| 框架    | JUnit 5 Jupiter、Mockito、Spring MockMvc                                              |
| 命令    | `./gradlew :febit-boot:test`（`make test` 跑全量）                                    |
| Profile | `unit-test`（`boot/src/test/resources/application-unit-test.yml`）                    |

