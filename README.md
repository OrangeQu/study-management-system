# Study Management System
一站式学习管理平台，面向学生用户提供任务/计划可视化、学习趋势分析、成绩管理、个性化设置、安全管理与可导出的学习数据；前端使用 Vue 3 + Element Plus 构建，后端基于 Spring Boot + MySQL 通过 JWT 提供 REST 接口。

## 目录
1. [核心功能](#核心功能)
2. [技术与架构](#技术与架构)
3. [仓库结构](#仓库结构)
4. [准备工作](#准备工作)
5. [本地启动](#本地启动)
6. [环境变量与配置](#环境变量与配置)
7. [可用脚本](#可用脚本)
8. [接口文档](#接口文档)
9. [部署建议](#部署建议)
10. [安全与维护](#安全与维护)
11. [贡献方式](#贡献方式)

## 核心功能
- **学习大盘**：`Dashboard`/`Study Center` 汇总任务统计、番茄番茄钟节奏、计划概览与学习趋势图，结合 Pinia、ECharts 和后端统计接口实现实时面板（参见 `src/views/Dashboard.vue` 与 `src/views/StudyCenter.vue`）。
- **任务/计划/设备管理**：任务支持 CRUD、筛选、批量清理；计划页面支持拖拽与时间范围操作；`Settings` 中可以查看登录设备并一键注销，所有数据通过 `/api/tasks`、`/api/plans`、`/api/user/devices` 等接口同步。
- **成绩管理**：`GradeData` 页面提供成绩列表、筛选、批量导入（CSV/XLSX）、选中导出与 GPA 趋势统计，后端 `/api/grades` 提供 CRUD、导入/导出、统计与 `导出` 接口生成真实 CSV 文件。
- **个性化设置**：允许用户修改头像、昵称、主题、番茄钟参数、学习目标、提醒策略与偏好配置（`/api/settings/preferences`）。管理面板可更新主题色变量并持久化到全局 CSS 变量。
- **鉴权与角色控制**：JWT 登录/注册、中间件自动载入用户资料，管理员额外拥有 `/api/admin/users` 接口以分页查询、修改角色、重置密码与删除账号（对应前端 `src/views/admin/UsersList.vue`）。
- **AI/数据备份**：提供 DeepSeek 代理聊天接口与 `/api/data/export`、`/api/data/history` 用于导出/清理学习数据。

## 技术与架构
- **前端：** Vue 3 + Vue Router 4 + Pinia + Element Plus + Axios + Day.js + ECharts + vuedraggable。
- **后端：** Spring Boot 3.3 + Spring Security + JWT + Spring Data JPA + MySQL + Lombok + Jackson + Apache POI（用于成绩导入）。
- **工程工具：** Vue CLI、Maven、Node.js 18+/npm 9+、ESLint（Vue essential 规则）、OpenJDK 17。
- **状态管理与路由保护：** 前端通过 Pinia 持久化用户信息与偏好，路由守卫在 `src/router/index.js` 检查 Token 并处理管理员权限。
- **安全网关：** 后端通过 `JwtAuthenticationFilter` 处理 Bearer Token，`@PreAuthorize("hasRole('ADMIN')")` 保护管理接口。

## 仓库结构
```
study-management-system/
├── public/                       # 静态资源
├── src/                          # Vue SPA
│   ├── api/                      # Axios 请求封装，按 domain 划分（auth/user/grades/tasks/data）
│   ├── components/               # 公共组件（布局、表格、模态框、GPA 模块等）
│   ├── router/                   # 路由配置与守卫
│   ├── stores/                   # Pinia store：用户、偏好、任务缓存等
│   └── views/                    # 页面层：Dashboard、StudyCenter、GradeData、Settings、Auth、admin
├── server-java/                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/...         # controller/service/repository/model/security
│   └── src/main/resources/       # application.yml、静态配置
├── API.md                        # REST API 总览（认证、任务、成绩、学习记录等）
├── README.md                     # 当前文档
```

## 准备工作
- 安装 Node.js 18+ 与 npm 9+，用于前端依赖与构建。
- 安装 Java 17 与 Maven 3.9+，用于后端服务。
- 准备 MySQL 8+ 数据库，默认数据库名 `study_management`（可通过 `.env` 自定义）。
- 可选安装 Postman/HTTP 客户端用于调试 API，Element Plus 组件文档可帮助 UI 调试。

## 本地启动

### 前端（Vue）
1. 安装依赖：`npm install`
2. 设置 API 基础地址（建议创建 `.env.local` 并添加）：
   ```env
   VUE_APP_API_BASE=http://localhost:8080/api
   ```
3. 启动开发服务器：`npm run serve`
4. 编译发布包：`npm run build`
5. 静态校验：`npm run lint`

### 后端（Spring Boot）
1. 复制环境模板：`cp server-java/.env.example server-java/.env`，替换为实际值（参考下一节）。
2. 启动服务：`cd server-java && mvn spring-boot:run`
3. 默认接口基准：`http://localhost:8080/api`

## 环境变量与配置
- `server-java/.env`（建议忽略到 `.gitignore`，实际关键信息应以环境变量注入）：
  ```
  DB_URL=jdbc:mysql://localhost:3306/study_management?useSSL=false&serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true
  DB_USERNAME=root
  DB_PASSWORD=<your-password>
  JWT_SECRET=<secure-secret>
  JWT_EXPIRES=7d
  DEEPSEEK_API_KEY=<your-deepseek-key>
  DEEPSEEK_API_BASE=https://api.deepseek.com
  SERVER_PORT=8080
  ```
- `server-java/src/main/resources/application.yml` 通过 `spring.config.import=optional:file:.env[.properties]` 自动载入以上变量。
- `VUE_APP_API_BASE` 控制前端请求的基础路径，可指向本地开发或线上 API。
- 可以通过 `node_config`/`profiles` 进一步覆盖（例如 `npm run serve -- --mode staging`）。

## 可用脚本
| 目录 | 命令 | 说明 |
| --- | --- | --- |
| `/` | `npm run serve` | 启动 Vue 本地开发服务器 |
| `/` | `npm run build` | 生成 `dist/` 生产包 |
| `/` | `npm run lint` | 运行 ESLint 检查 |
| `server-java/` | `mvn spring-boot:run` | 启动 Spring Boot API |
| `server-java/` | `mvn test` | 运行后端测试（如有） |

## 接口文档
- 所有 REST 接口详见 [API.md](API.md)，包含：
  - 认证与用户（`/api/auth/*`、`/api/user/*`）
  - 管理员用户（`/api/admin/users/*`）
  - 任务/计划（`/api/tasks`、`/api/plans`）
  - 成绩（`/api/grades`：CRUD、导入、导出、统计）
  - 学习记录（`/api/study/*`）、数据导出/清理、设置偏好与 AI 聊天。
- 所有 API 返回统一 `{ code, message, data }` 结构，认证使用 `Authorization: Bearer <token>`。

## 部署建议
- 前端：执行 `npm run build`，将 `dist/` 部署到 Nginx 或静态服务，配置 `VUE_APP_API_BASE` 指向生产后端。
- 后端：使用 Maven 打包并运行 `java -jar`，确保数据库与 JWT secret 通过环境变量注入；推荐使用连接池与 HTTPS。
- 可在 Nginx 前端配置代理 `/api` 到后端，避免跨域。
- 生产环境务必开启 HTTPS，限制登录频率与鉴权时效。

## 安全与维护
- `.env` 文件建议写入 `.gitignore`，不要将数据库密码、JWT secret 与 DeepSeek key 提交到仓库；使用 `.env.example` 作为模板。
- 定期更换 JWT secret 和数据库账号密码，保持后端 `spring.jpa.hibernate.ddl-auto` 仅在开发模式可设置为 `update`，生产环境建议显式管理迁移（Flyway/Liquibase）。
- 建议在后端或 API 网关处启用请求限流，防止机器人枚举 `/api/auth`。
- 推荐在部署前运行 `npm run lint` + `npm run build` 和 `mvn test`（后端尚需补充测试）。

## 贡献方式
- 使用 Git Flow：在 `main` 之外创建 feature 分支，完成开发后提交 Pull Request。
- 说明变更是什么、为什么需要，并附上测试步骤；如需 API 调用示例，可结合 [API.md](API.md)。
- 发现问题请先尝试复现并收集控制台、网络与后端日志，再在 issue 中附上重现步骤与环境信息。
