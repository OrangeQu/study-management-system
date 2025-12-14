# study-management-system

## 项目简介
Study Management System 是一个面向学生的全栈学习管理平台。前端使用 Vue 3 + Element Plus 构建单页应用，提供学习看板、任务/计划管理、GPA 数据可视化、个性化设置等功能；后端基于 Spring Boot 3 + MySQL，提供涵盖认证、任务、计划、成绩、学习时长、偏好设置、设备安全等 REST API，目标是一站式管理学习计划与结果。

## 主要功能
- **统一学习工作台**：`/dashboard` 与 `/study-center` 整合番茄钟、任务 CRUD、学习计划、待办提醒、AI 学习助手和实时统计（参见 `src/views/Dashboard.vue`、`src/views/StudyCenter.vue`）。
- **成绩与可视化**：`/grade-data` 支持 GPA 汇总、学期过滤、成绩录入/导入/导出、趋势图表（后端 `GradeController` + 前端 `src/components/gpa/*`）。
- **个性化与安全**：`/settings` 可修改个人资料、切换主题、配置番茄钟、管理登录设备、导出/清理数据（涉及 `/api/user`、`/api/settings`、`/api/data`、`/api/tasks`）。
- **后端服务**：`server-java` 模块内含 JWT 认证、设备记录、各类 CRUD 仓储、学习统计、数据导入导出等业务逻辑，路径 `server-java/src/main/java/com/example/studymanagement`。

## 技术栈
- **前端**：Vue 3、Vue Router 4、Pinia、Element Plus、Axios、Day.js、ECharts、vuedraggable。
- **后端**：Spring Boot 3.3、Spring Security + JWT、Spring Data JPA、MySQL、Lombok、Jackson、Maven。
- **工具链**：Node.js 18+ / npm 9+、Java 17、Maven 3.9+、ESLint（Vue essential 规则）。

## 目录结构
```
study-management-system
├── public/                         # 前端静态资源
├── src/                            # Vue 3 SPA
│   ├── api/                        # Axios 请求封装（任务/计划/成绩/用户/聊天/设置）
│   ├── components/                 # 可复用组件（布局、图表、任务面板、GPA 组件等）
│   ├── router/                     # 路由与守卫
│   ├── stores/                     # Pinia 主 store（用户信息、偏好、任务缓存）
│   └── views/                      # 页面组件（Dashboard、StudyCenter、GradeData、Settings、Auth）
├── server-java/                    # Spring Boot 后端
│   ├── pom.xml                     # 依赖声明
│   └── src/main/java/...           # Controller / Service / Repository / Security / DTO
├── API.md                          # REST 接口与环境说明
├── package.json                    # 前端脚本与依赖
└── README.md
```

## 快速开始

### 前端（Vue 客户端）
1. 安装依赖：`npm install`
2. 根据需要创建 `.env.local` 覆盖后端地址（默认 `http://localhost:8080/api`，见 `src/api/http.js`）  
   ```
   VUE_APP_API_BASE=http://localhost:8080/api
   ```
3. 启动开发环境：`npm run serve`
4. 代码校验：`npm run lint`
5. 构建生产包：`npm run build`（输出目录 `dist/`）

### 后端（Spring Boot）
1. 安装 Java 17+ 与 Maven 3.9+
2. 准备 MySQL 数据库（默认库名 `study_management`，可在 `application.yml` 中调整）
3. 建议在 `server-java` 目录下复制 `.env.example` 为 `.env`（或直接设置系统环境变量）覆盖默认凭据，常用变量如下：
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/study_management?useSSL=false&serverTimezone=Asia/Shanghai
   SPRING_DATASOURCE_USERNAME=<your-user>
   SPRING_DATASOURCE_PASSWORD=<your-password>
   JWT_SECRET=<random-256-bit-string>
   JWT_EXPIRES=7d
   DEEPSEEK_API_KEY=<api-key>
   DEEPSEEK_API_BASE=https://api.deepseek.com
   ```
   Spring Boot 已在 `application.yml` 中通过 `spring.config.import=optional:file:.env[.properties]` 自动加载 `.env`，无需额外插件。
4. 启动服务：
   ```
   cd server-java
   mvn spring-boot:run
   ```
5. 默认监听 `http://localhost:8080/api`；`spring.jpa.hibernate.ddl-auto=update` 会自动建表，可通过 `/api/auth/register` 创建测试账号。

## API 文档
详细接口说明请参考 [API.md](API.md)，包含认证、任务/计划 CRUD、成绩统计及导入导出、学习记录、偏好设置、数据导出和 AI 聊天代理协议。

## 未完成 / 待补足事项
1. **AI 聊天真实接入**：`ChatController` 已调用 DeepSeek API（参考 `DeepseekService`），只需在 `server-java/.env` 中配置 `DEEPSEEK_API_KEY` 与 `DEEPSEEK_API_BASE`，即可在本地体验实时 AI 回复。
2. **学习统计未接入真实数据**：`StudyChart` 仍使用硬编码数组，未调用 `/api/study/stats/trend`；`PomodoroTimer` 也没有把专注时长上报 `/api/study/sessions`，导致后端学习统计始终为空。
3. **敏感配置直接写入仓库**：`server-java/src/main/resources/application.yml` 里包含数据库口令、JWT Secret、DeepSeek Key，须尽快迁移到环境变量或安全配置中心。
4. **数据库结构缺乏沉淀**：项目未引入 Liquibase/Flyway，也没有 SQL 初始化脚本，新环境完全依赖 Hibernate 自动建表，缺少索引、示例数据说明。
5. **缺少测试与 CI**：前后端都没有单元/集成测试，也没有自动化流程去执行 `npm run build` / `mvn test`，在上线前需要补齐关键路径的测试与流水线。

README 会随着功能完善持续更新，欢迎在完成上述事项后补充最新状态。
