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
1. **设置页文案全部乱码**：`src/views/Settings.vue` 中大量表单提示、校验文案与消息文本已经被 `????` 取代，发布后页面会显示乱码，用户无法理解提示。需要修复文件编码或文案源头，再补齐正确的中英文提示。
2. **路由守卫强制跳过认证**：`src/router/index.js:45` 直接在 `beforeEach` 内写死 `localStorage.setItem('DISABLE_AUTH','true')`，导致所有受保护页面无需登录即可访问，上线前必须去掉该语句并通过环境变量或构建配置来控制开发模式绕过。
3. **成绩导出仍是占位**：`src/views/GradeData.vue:755` 的 `exportData` 仅弹出“导出功能开发中...”提示，未调用 `src/api/grades.js` 的 `exportGrades` 接口。需要实现真实的文件下载、选中行导出以及失败提示。
4. **敏感凭据直接随仓库下发**：`server-java/.env` 当前带有真实的数据库账号、JWT Secret 与 DeepSeek API Key，仓库一旦共享这些凭据就会失效。需要立刻撤换密钥、停止追踪 `.env`，改用 `.env.example + 环境变量` 与安全密钥管理。
5. **数据库结构缺乏版本管理**：后端依赖 `spring.jpa.hibernate.ddl-auto=update` 自动建表，没有 Flyway/Liquibase 或 SQL 迁移脚本，新环境的表结构、索引、示例数据均不可追溯，后续演进与回滚会非常困难。
6. **测试与流水线完全缺失**：前端无单元/端到端测试，后端 `server-java` 也没有 `src/test/java`，并且仓库中没有任意 CI（如 GitHub Actions）去执行 `npm run build`、`npm run lint`、`mvn test`。发布前需要补齐最基础的回归用例和自动化构建。

README 会随着功能完善持续更新，欢迎在完成上述事项后补充最新状态。

## 成绩批量导入

- 模板字段：课程代码、课程名称、学分、成绩（0-100）为必填，绩点（可留空自动折算）、课程类型（必修/选修/通识/实践）、学期、学年为可选。
- 支持 CSV 与 Excel（.xlsx/.xls）文件，第一行必须是表头，使用 UTF-8 编码。
- 在 GPA 数据面板点击“批量导入”可以下载模板并上传文件，系统会校验每一行并提示导入/失败数量及原因。
