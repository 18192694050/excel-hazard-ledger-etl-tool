# excel-hazard-ledger-etl-tool
地质灾害隐患点台账Excel导入ETL管理系统

## 📖 项目简介
本项目基于 SpringBoot 开发，面向地质灾害台账业务场景，实现隐患点Excel台账批量导入、数据校验、空间信息入库、导入结果汇总导出等ETL能力。依托 PostgreSQL + PostGIS 存储隐患点空间坐标数据，支撑台账规范化管理，适用于地质灾害隐患信息汇总、数据清洗入库业务场景。

> 说明：仓库内所有业务敏感数据均已脱敏，仅保留工程代码与业务逻辑，不包含真实生产配置与原始台账数据。

## 🛠️ 技术栈
- 后端框架：SpringBoot 2.x
- 数据库：PostgreSQL + PostGIS（空间地理扩展）
- Excel解析：Apache POI
- 构建工具：Maven
- 开发环境：JDK17
- GIS配套工具：QGIS（外部用于空间数据可视化核查）

## ✨ 核心功能
1. Excel台账批量上传解析，支持标准模板导入地质灾害隐患点信息
2. 多层次数据校验：字段非空校验、数据格式校验、隐患编号重复校验
3. 隐患点经纬度空间信息入库，基于PostGIS存储地理坐标
4. 导入任务结果统计，支持导入成功/失败数据汇总展示
5. 异常数据拦截，记录错误原因，便于台账修正重导
6. 分层代码架构：Controller → Service → Mapper，符合SpringBoot开发规范

## 📂 项目目录结构
excel-hazard-ledger-etl-tool
├── doc # 项目文档、接口说明、业务流程图、数据库设计文档
├── src/main
│ ├── java/com/example
│ │ ├── controller # 接口控制器
│ │ ├── entity # 数据库实体类
│ │ ├── mapper # Mybatis 数据访问层
│ │ ├── service # 业务逻辑层
│ │ ├── vo # 请求 / 返回视图对象
│ └── resources # 配置文件、Mapper XML
├── .gitignore # Git 忽略文件配置
├── pom.xml # Maven 依赖配置
├── mvnw /mvnw.cmd # Maven 包装脚本
└── README.md # 项目说明文档

## 🚀 本地运行步骤
1. 环境准备
   - JDK 17
   - PostgreSQL 数据库，启用 PostGIS 扩展
2. 修改配置
   编辑 `src/main/resources/application.yml`，配置数据库连接信息
3. 初始化数据表
   执行doc目录下脱敏后的数据库建表SQL脚本
4. 项目启动
   ```bash
   # 使用Maven启动
   mvn spring-boot:run
