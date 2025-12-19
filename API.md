后端接口与配置说明（基于当前前端功能）

## 环境与连接
- 数据库：MySQL（示例在 .env 中配置）
```
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your-db-password
DB_NAME=study_management

JWT_SECRET=please_change_me
JWT_EXPIRES=7d

DEEPSEEK_API_KEY=your-deepseek-key
DEEPSEEK_API_BASE=https://api.deepseek.com
```
- 统一返回：`{ code, message, data }`
- 鉴权：JWT，HTTP 头 `Authorization: Bearer <token>`
- 时间格式：`YYYY-MM-DD HH:mm:ss`
- 分页：`page`（默认 1），`pageSize`（默认 10/20）

## 数据表建议（简要）
- users：id, username, password_hash, email, nickname, gender, phone, avatar, role, created_at, updated_at
- login_devices：id, user_id, device_name, location, last_login, is_current, created_at
- tasks：id, user_id, title, description, type, priority, deadline, course, status(todo/doing/done), completed, tags(JSON), created_at, updated_at
- plans：id, user_id, time_start, time_end, task_title, task_id, course, description, status, date, created_at, updated_at
- grades：id, user_id, course_code, course_name, credit, score, grade_point, course_type, semester, academic_year, status, is_required, created_at, updated_at
- study_sessions：id, user_id, type(pomodoro/study), duration_minutes, mode(work/break), task_id, plan_id, started_at, end_at, status(running/completed/aborted), created_at
- settings：id, user_id, preferences(JSON，含 theme、pomodoroWork/Break、taskReminder、dailyGoal、weeklyGoal、uiFlags 等)，created_at, updated_at

## 认证与用户
- POST /api/auth/register  
  - body：{ username, password, email }  
  - return：{ token, user }
- POST /api/auth/login  
  - body：{ username, password }  
  - return：{ token, user }
- POST /api/auth/logout  
  - 功能：注销当前 token（可选记录黑名单/版本号）
- POST /api/auth/refresh  
  - return：{ token }
- GET /api/user/me  
  - return：当前用户信息
- PUT /api/user/profile  
  - body：{ nickname, gender, phone, email }
- PUT /api/user/password  
  - body：{ currentPassword, newPassword }
- POST /api/user/avatar  
  - form-data：file
- GET /api/user/devices  
  - return：登录设备列表
- DELETE /api/user/devices/{id}  
  - 功能：下线指定设备

## 管理员操作
- 所有 `/api/admin/users/**` 接口受 `@PreAuthorize("hasRole('ADMIN')")` 保护，仅管理员可访问，统一返回 `{ code, message, data }`。
- GET /api/admin/users  
  - query：`page`、`pageSize`、`q`（用户名或邮箱模糊）  
  - return：`{ list:[{id,username,nickname,email,role}], total, page }`
- POST /api/admin/users  
  - body：`{ username, password, nickname?, email?, role? }`（role 以 `admin`/`user` 区分）  
  - return：新建用户信息（除密码外）
- PUT /api/admin/users/{id}  
  - body：可选 `{ username, password, nickname, email, role }`，会校验唯一性
- PUT /api/admin/users/{id}/password  
  - body：`{ password }`，允许管理员重置密码
- DELETE /api/admin/users/{id}  
  - 功能：删除用户记录

## 任务 Task
- GET /api/tasks  
  - query：search, status(todo/doing/done), priority(1/2/3), type, deadline_from, deadline_to, sort, page, pageSize
- POST /api/tasks  
  - body：{ title, description, type, priority, deadline, course, status, completed, tags }
- PUT /api/tasks/{id}  
  - body：同上
- PATCH /api/tasks/{id}/status  
  - body：{ status, completed }
- DELETE /api/tasks/{id}
- DELETE /api/tasks/completed  
  - 功能：清理已完成任务
- GET /api/tasks/stats/summary  
  - return：{ todayTodo:[], urgent:[], completedCount, totalCount, completionRate }

## 学习计划 Plan
- GET /api/plans  
  - query：date=YYYY-MM-DD
- POST /api/plans  
  - body：{ time_start, time_end, task_title, task_id?, course, description, status }
- PUT /api/plans/{id}  
  - body：同上
- PATCH /api/plans/{id}/status  
  - body：{ status }
- DELETE /api/plans/{id}

## 成绩 GPA
- GET /api/grades  
  - query：keyword, semester, academic_year, course_type, status, sort(semester_desc/semester_asc/score_desc/gpa_desc), page, pageSize
- POST /api/grades  
  - body：{ course_code, course_name, credit, score, grade_point?, course_type, semester, academic_year, status }
- PUT /api/grades/{id}  
  - body：同上
- DELETE /api/grades/{id}
- POST /api/grades/import  
  - form-data：file(xlsx/csv)，首行必须为表头，至少包含 `课程代码/课程名称/学分/成绩(0-100)`，可选 `绩点/课程类型/学期/学年`  
  - return：{ imported, failed, errors:[逐行错误提示] }
- GET /api/grades/export  
  - query：ids?（可选）导出所选或全部
- GET /api/grades/stats  
  - return：{ currentSemesterGPA, totalGPA, passRate, totalCredits, passedCredits, totalCourses, passedCourses, trend:[{semester,gpa}] }

## 学习记录 / 计时
- POST /api/study/sessions
  - body：{ type:'pomodoro'|'study', duration_minutes, mode(work/break), task_id?, plan_id?, started_at?, status? } —— 直接写入一条学习记录
- POST /api/study/sessions/start
  - body：{ type, mode, duration_minutes }
  - return：{ id, started_at, end_at }（番茄钟运行中会话）
- PATCH /api/study/sessions/{id}?action=complete|abort
  - body(action=complete)：{ duration_minutes, mode?, type? }
- GET /api/study/stats/today
  - return：{ studyMinutes, pomodoroCount }
- GET /api/study/stats/trend
  - query：range=7/30
  - return：按日/周时长序列
- GET /api/study/stats/overview
  - return：{ today_minutes, week_minutes, total_minutes, today_goal_minutes, week_goal_minutes, today_goal_completed, week_goal_completed, today_pomodoro_count, total_pomodoro_count, today_task_count, completed_task_count }

## AI 聊天（DeepSeek 占位）
- POST /api/chat/messages  
  - body：{ conversation_id?, content }  
  - return：{ conversation_id, reply, history? }  
  - 服务端用 `DEEPSEEK_API_KEY` 代理请求，避免前端暴露密钥。

## 设置 / 偏好与数据
- GET /api/settings/preferences  
  - return：preferences JSON
- PUT /api/settings/preferences  
  - body：{ theme, pomodoroWork, pomodoroBreak, taskReminder, dailyGoal, weeklyGoal, uiFlags... }
- GET /api/data/export  
  - query：type=all|grades|tasks
- DELETE /api/data/history  
  - 功能：清理历史记录

## 最小实现优先级
1) 认证/用户（注册、登录、密码、profile）+ 鉴权中间件。  
2) 任务 + 计划 CRUD + summary 统计（支撑 Dashboard/StudyCenter）。  
3) 成绩 CRUD + stats/export/import。  
4) 学习记录与趋势（StudyChart、番茄计时）。  
5) 设置偏好、设备管理、数据导出/清理、AI 聊天代理。  
