# sky-take-out

一个基于 Spring Boot 的外卖点餐后台管理系统，面向餐饮门店的日常运营场景，提供用户、菜品、套餐、订单、报表等核心管理能力。

## 项目简介

`sky-take-out` 是一个多模块 Maven 项目，整体采用前后端分离的工程结构，后端负责提供 REST API、业务逻辑处理、数据统计与导出能力。

项目当前包含以下模块：

- `sky-common`：公共工具类、通用配置、常量等
- `sky-pojo`：实体类、DTO、VO 等数据对象
- `sky-server`：核心业务服务、控制器、接口实现、配置文件

## 技术栈

- Spring Boot 2.7.3
- MyBatis / MyBatis-Plus 风格的数据访问能力
- MySQL
- Redis
- Druid 连接池
- Lombok
- Knife4j / Swagger 接口文档
- PageHelper 分页插件
- JWT 登录认证
- Apache POI 报表导出
- 阿里云 OSS 文件存储
- 微信支付相关组件

## 核心功能

- 员工登录与权限控制
- 用户与员工管理
- 菜品分类、菜品管理
- 套餐管理
- 订单处理与状态流转
- 营业数据统计
- 报表导出
- 文件上传与资源管理

## 报表能力

项目内置了较完整的数据统计与导出能力，可用于运营分析，例如：

- 营业额统计
- 用户统计
- 订单统计
- 热销商品 Top10
- 近 30 天营业数据 Excel 导出

例如 `ReportServiceImpl` 中实现了基于时间区间的统计查询，并使用 Apache POI 将统计结果导出到 Excel 模板，方便门店进行经营分析和数据归档。

## 项目结构

```text
sky-take-out
├── sky-common
├── sky-pojo
├── sky-server
└── pom.xml
```

## 运行环境

- JDK 8 或以上
- Maven 3.6+
- MySQL 5.7 / 8.0
- Redis

## 快速开始

1. 克隆项目

```bash
git clone git@github.com:luffy-111/sky-take-out.git
cd sky-take-out
```

2. 配置数据库与缓存

在 `sky-server/src/main/resources/application-dev.yml` 中配置数据库、Redis、OSS 等相关信息。

3. 导入数据库脚本

根据项目提供的 SQL 脚本初始化业务表和基础数据。

4. 启动后端服务

```bash
mvn clean package -DskipTests
java -jar sky-server/target/sky-server.jar
```

或者直接在 IDE 中启动 `sky-server` 模块。

## 接口文档

启动项目后，可通过 Knife4j / Swagger 页面查看接口文档。具体访问地址取决于项目中的配置，一般在后端启动成功后通过浏览器访问即可。

## 注意事项

- `application-dev.yml` 中通常包含本地开发所需的敏感配置，请勿直接提交真实生产密钥。
- 报表导出功能依赖模板文件，请确认资源目录下相关 Excel 模板已正确放置。
- 若数据库字段或表结构与代码不一致，统计接口和导出功能可能出现异常。

## 项目亮点

- 业务模块划分清晰，便于扩展和维护
- 统计报表能力较完整，适合展示运营数据
- 使用模板导出 Excel，提升报表可读性
- 适合作为 Spring Boot + MyBatis 课程/实战项目展示
