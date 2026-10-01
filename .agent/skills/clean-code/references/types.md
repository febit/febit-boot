# types — 类型即护栏（Java 严格类型范式）

> 用类型在编译期挡住混用与漏分支，运行期零开销。写强类型模型 / 状态机时读。

## 总原则

类型不是装饰，是 **护栏**：状态机用 sealed + 穷尽匹配，编译期拦混用与漏分支；
禁止用裸 `if/else` 覆盖状态机。

## Java 类型范式

- **空安全分层**——`@NullMarked` 策略（逻辑包 / 数据载体包分别怎么标、`@Nullable` 与
  `@lombok.NonNull` 如何选）唯一权威见 [coding-java.md](../../../guides/coding-java.md)，本文件不重复。
- **sealed 类型化层级**——通用事件 / 结果用 `sealed interface` + record 实现
  （贫血、构造器只收终值）；新增子类由编译器强制处理，且 **不写 `default`**
  （见 [coding-java](../../../guides/coding-java.md)）。
- **判别穷尽**——`switch` 模式匹配由编译器兜底；`default` 会掩盖新增分支的编译错误。
- **空安全**——`Objects.equals` 避免 NPE；查询类方法可返回 `Optional`。
- **包装类型区分「未传」与「显式值」**——入参字段不用基本类型
  （见 [coding-java](../../../guides/coding-java.md)）。

## 参考

- 后端红线（空安全 / 命名 / 禁止项）：[coding-java.md](../../../guides/coding-java.md)
