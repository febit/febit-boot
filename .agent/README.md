# .agent — 目录索引

> 编写规范：指南见 [guides/README.md](guides/README.md)，skill 见 [skills/authoring.md](skills/authoring.md)。
> 「何时加载哪个文档」的路由见 [AGENTS.md](../AGENTS.md)（唯一权威）。

## 概念指南

| 指南                                     | 覆盖                                 |
|------------------------------------------|--------------------------------------|
| [coding-java](guides/coding-java.md)     | 空安全 / 命名 / 风格 / 卫语句 / 禁令 |
| [code-comment](guides/code-comment.md)   | 注释：原则 / 两类 / 三禁令 / 两分档  |
| [reliability](guides/reliability.md)     | 幂等 CAS / 错误处理 / 日志           |
| [testing](guides/testing.md)             | 测试继承体系 / 组织 / 命名           |
| [documentation](guides/documentation.md) | 文档只记稳定之物                     |

> 归属： **怎么用**归 `.agent`（guide 红线 / skill 用法 / templates 模板）； **为什么这样取舍**归 `doc/design/`。

## 模板

文档模板在 [templates/](templates/README.md)（index / design），唯一索引见该目录 README。

## Skills

清单见 [skills/README.md](skills/README.md)，路由见 [AGENTS.md](../AGENTS.md)。

## 维护约定

- 改目录结构 / 增删文件后，自检 **Markdown 链接**（改名 / 移目录后断链）。
- 新增索引项必须是可跳转链接——纯文本不可校验，Agent 也拼不出路径。
- 每份文档须能从 [AGENTS.md](../AGENTS.md) 经链接走到，否则等于加载不到。
