# Akhmatova

前后端分离项目：Spring Boot 4 后端 + Vite + React 前端，PostgreSQL 存储用户与登录信息。

## 环境要求

| 组件 | 版本建议 |
|------|-----------|
| JDK | 17 |
| Node.js | 20+（或当前 LTS） |
| npm | 随 Node 自带 |
| PostgreSQL | 14+（本地或远程实例） |

## 从 Git 克隆后首次运行

### 1. 数据库

1. 启动 PostgreSQL，创建或使用已有库（默认示例为库名 `postgres`）。
2. 建表（与 JPA 实体一致）：

   ```bash
   psql -U postgres -d postgres -f database_config/schema.sql
   ```

3. 配置连接与密码（**不要提交**真实密码到 Git）：

   ```bash
   cp database_config/database.properties.example database_config/database.properties
   ```

   编辑 `database_config/database.properties`，填写 `spring.datasource.password` 等字段。

   也可使用环境变量（与 `.env.example` 中一致），例如：

   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=你的密码
   ```

   可选：复制 `database_config/.env.example` 为 `database_config/.env`，在本地 shell 中 `source` 后再启动后端。

### 2. 后端（必须在 `backend` 目录下启动）

后端通过 `spring.config.import=optional:file:../database_config/database.properties` 读取与 `backend` **同级**的 `database_config/database.properties`。因此 **工作目录必须是 `backend/`**（Maven、IDE 运行配置的工作目录也应设为 `backend`）。

```bash
cd backend
```

- Windows（PowerShell / CMD）：`.\mvnw.cmd spring-boot:run`
- Linux / macOS：`./mvnw spring-boot:run`
- 默认监听 **http://localhost:8080**。
- 单元测试使用 H2，不依赖 PostgreSQL：

  ```bash
  .\mvnw.cmd test
  ```

  （Linux / macOS 使用 `./mvnw test`。）

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

- 开发服务器默认 **http://localhost:5173**。
- `vite.config.js` 已将 `/api` 代理到 `http://localhost:8080`，请保证后端已启动。

### 4. 登录与注册

- 登录页：`/login`（或根路径由路由决定）。
- 注册页：`/register`。
- 首次注册会在库中写入 `user_information` 与 `login`（密码为 BCrypt）；详见后端代码与 `database_config/schema.sql`。

## 目录说明

| 路径 | 说明 |
|------|------|
| `backend/` | Spring Boot 工程，`src/main/resources/application.properties` 为默认配置 |
| `frontend/` | Vite + React 工程 |
| `database_config/` | 本地数据库属性模板、`.env` 示例、建表 SQL；敏感文件见该目录下 `.gitignore` |

## 忽略提交的文件（根 `.gitignore` 摘要）

- 各 IDE：`.idea/`、`.vscode/` 等  
- 前端：`frontend/node_modules/`、`frontend/dist/` 等  
- 后端：`backend/target/` 等  
- 密钥与本地配置：`database_config/database.properties`、`database_config/.env`、根目录 `.env`  

子目录内另有 `backend/.gitignore`、`frontend/.gitignore`，与根规则互补。

## 常见问题

1. **后端启动报数据源 / 密码错误**  
   确认已创建 `database_config/database.properties` 或已设置 `SPRING_DATASOURCE_*` 环境变量，且从 **`backend` 目录** 启动。

2. **前端请求 404 或连不上 API**  
   确认后端在 8080 运行；开发环境应通过 Vite 访问页面（走代理），不要只打开静态文件。

3. **表已存在或结构不一致**  
   开发环境 JPA 使用 `ddl-auto=update` 会尝试同步实体；生产环境请使用迁移工具或手工维护 `schema.sql` 与实体一致。
