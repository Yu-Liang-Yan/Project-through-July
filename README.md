# 守护宝贝 - 智能监护系统

面向18岁以下未成年人家庭的智能监护与设备使用管理平台，支持**跨平台、多设备、去中心化数据存储**。

---

## 一、本地化代码（GitHub 新手友好指南）

### 方法1：直接下载 ZIP（最简单，无需安装 Git）

1. 打开仓库主页：`https://github.com/Yu-Liang-Yan/Project-through-July`
2. 点击绿色按钮 **「Code」** → 选择 **「Download ZIP」**
3. 将下载的 ZIP 文件解压到任意目录（如 `D:\guardian-baby\`）

### 方法2：通过 Git 克隆（推荐，可接收后续更新）

```bash
# 1. 先安装 Git：https://git-scm.com/download/win （一路「Next」即可）
# 2. 打开 PowerShell 或 cmd，执行：

git clone https://github.com/Yu-Liang-Yan/Project-through-July.git

# 3. 代码会下载到当前目录下的 Project-through-July 文件夹中
```

> 如果已经下载过，想拉取最新代码：
> ```bash
> cd Project-through-July
> git pull origin master
> ```

---

## 二、完整运行和测试方法

### 环境要求

| 工具 | 版本 | 下载地址 |
|------|------|----------|
| **Java JDK** | 17+ | [Adoptium JDK 17](https://adoptium.net/download/) |
| **Maven** | 3.8+ | 项目自带 Maven Wrapper（通过 `mvnw` 运行），无需单独安装 |
| **Node.js** | 18+ | [Node.js 官网](https://nodejs.org/)（推荐 LTS 版本） |
| **Git**（可选） | - | 仅克隆代码时需要 |

> **安装提示**：Java 和 Node.js 安装后需要**重启终端**才能生效。可用 `java -version` 和 `node -v` 验证安装。

---

### 2.1 启动后端（Spring Boot）

```bash
# 进入后端目录
cd Project-through-July/backend

# 方式一：使用项目自带的 Maven Wrapper（推荐，无需安装 Maven）
# Windows PowerShell:
.\mvnw.cmd spring-boot:run

# Windows cmd:
mvnw spring-boot:run

# macOS / Linux:
./mvnw spring-boot:run

# 方式二：如果你已安装 Maven
mvn spring-boot:run
```

**启动成功标志**：
```
Tomcat started on port 8080 (http)
Started GuardianBabyApplication in 4.083 seconds
演示数据初始化完成
```

**验证后端是否正常**（新开一个终端运行）：

```powershell
# PowerShell 中执行：
$body = '{"username":"admin","password":"123456"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body
```

如果返回 JSON 中包含 `"success": true` 和 `"token"` 字段，说明后端运行正常。

---

### 2.2 启动前端（Vue 3）

```bash
# 新开一个终端，进入前端目录
cd Project-through-July/frontend

# 安装依赖（仅首次需要，约 2-3 分钟）
npm install

# 启动开发服务器
npm run dev
```

**启动成功标志**：
```
VITE v5.x.x  ready in xxx ms
➜  Local:   http://localhost:5173/
```

浏览器访问 `http://localhost:5173/`，使用演示账号登录：

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | `123456` |

---

### 2.3 测试 API 接口

后端启动后可访问以下端点验证：

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 登录 | POST | `/api/auth/login` | 传入 `{"username":"admin","password":"123456"}` |
| 注册 | POST | `/api/auth/register` | 传入注册信息 JSON |
| 设备列表 | GET | `/api/devices?userId=1` | 需携带 JWT Token |
| 仪表盘 | GET | `/api/statistics/dashboard?userId=1` | 需携带 JWT Token |

**完整测试流程（PowerShell）**：

```powershell
# 1. 登录获取 token
$body = '{"username":"admin","password":"123456"}'
$r = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body
$token = $r.data.token

# 2. 查询设备列表
Invoke-RestMethod -Uri "http://localhost:8080/api/devices?userId=1" -Headers @{Authorization="Bearer $token"}

# 3. 查询禁止列表
Invoke-RestMethod -Uri "http://localhost:8080/api/block-list?userId=1&type=games" -Headers @{Authorization="Bearer $token"}

# 4. 查询使用记录
Invoke-RestMethod -Uri "http://localhost:8080/api/statistics/usage?userId=1&period=week" -Headers @{Authorization="Bearer $token"}
```

---

### 2.4 构建生产版本

```bash
# 前端构建
cd frontend
npm run build
# 产物在 frontend/dist/ 目录，可直接部署到 Nginx / CDN

# 后端构建
cd backend
.\mvnw.cmd package -DskipTests
# 产物在 backend/target/guardian-baby-backend-1.0.0.jar
# 运行: java -jar target/guardian-baby-backend-1.0.0.jar
```

---

### 2.5 数据库切换

开发环境默认使用 **H2 内存数据库**（无需安装）。切换到 MySQL：

```bash
# 方式一：修改 application.yml 中的 spring.profiles.active 为 mysql
# 方式二：启动时指定
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

MySQL 需先创建数据库：
```sql
CREATE DATABASE guardian_baby DEFAULT CHARACTER SET utf8mb4;
```

---

## 三、项目层级结构

```
guardian-baby/
│
├── README.md                        # 项目说明文档（本文件）
├── .gitignore                       # Git 忽略规则
│
├── backend/                         # ███ 后端 - Spring Boot 3.2 ███
│   ├── pom.xml                      #   Maven 依赖配置
│   └── src/main/
│       ├── resources/
│       │   └── application.yml      #   Spring Boot 配置（含 H2/MySQL 双 Profile）
│       └── java/com/guardianbaby/
│           │
│           ├── GuardianBabyApplication.java   # 启动入口
│           │
│           ├── common/               # ─── 通用模块 ───
│           │   └── exception/
│           │       ├── BusinessException.java       # 业务异常
│           │       └── GlobalExceptionHandler.java  # 全局异常处理
│           │
│           ├── config/               # ─── 配置模块 ───
│           │   ├── CorsConfig.java          # 跨域配置
│           │   ├── SecurityConfig.java      # Spring Security 安全配置
│           │   ├── JwtUtil.java             # JWT Token 生成/校验
│           │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│           │   └── DataInitializer.java     # 演示数据初始化
│           │
│           ├── entity/               # ─── 实体层（JPA 映射）───
│           │   ├── User.java         #    用户（监护人/被保护对象）
│           │   ├── Device.java       #    设备（手机/平板/电脑/手表/阅读器）
│           │   ├── TimeSettings.java #    时间控制设置
│           │   ├── BlockItem.java    #    禁止列表项
│           │   └── UsageRecord.java  #    使用记录
│           │
│           ├── dto/                  # ─── 数据传输对象 ───
│           │   ├── ApiResponse.java         # 统一响应格式 {success, message, data}
│           │   ├── LoginRequest.java        # 登录请求
│           │   ├── LoginResponse.java       # 登录响应（含 JWT Token）
│           │   ├── RegisterRequest.java     # 注册请求
│           │   ├── UserResponse.java        # 用户信息响应
│           │   ├── DeviceResponse.java      # 设备信息响应
│           │   ├── TimeSettingsResponse.java# 时间设置响应
│           │   ├── BlockItemResponse.java   # 禁止项响应
│           │   ├── UsageRecordResponse.java # 使用记录响应
│           │   └── DashboardResponse.java   # 仪表盘数据响应
│           │
│           ├── repository/           # ─── 数据访问层（Spring Data JPA）───
│           │   ├── UserRepository.java
│           │   ├── DeviceRepository.java
│           │   ├── TimeSettingsRepository.java
│           │   ├── BlockItemRepository.java
│           │   └── UsageRecordRepository.java
│           │
│           ├── service/              # ─── 业务逻辑层 ───
│           │   │                     #   接口定义
│           │   ├── AuthService.java
│           │   ├── DeviceService.java
│           │   ├── TimeSettingsService.java
│           │   ├── BlockListService.java
│           │   ├── StatisticsService.java
│           │   │
│           │   └── impl/             #   接口实现
│           │       ├── AuthServiceImpl.java
│           │       ├── DeviceServiceImpl.java
│           │       ├── TimeSettingsServiceImpl.java
│           │       ├── BlockListServiceImpl.java
│           │       └── StatisticsServiceImpl.java
│           │
│           └── controller/           # ─── 控制层（REST API）───
│               ├── AuthController.java          # /api/auth/*      登录/注册
│               ├── DeviceController.java        # /api/devices/*   设备 CRUD
│               ├── TimeSettingsController.java  # /api/time-settings/* 时间设置
│               ├── BlockListController.java     # /api/block-list/*   禁止列表
│               └── StatisticsController.java    # /api/statistics/*   统计查询
│
├── frontend/                         # ███ 前端 - Vue 3 + TypeScript ███
│   ├── index.html                   #   HTML 入口
│   ├── package.json                 #   Node 依赖配置
│   ├── vite.config.ts               #   Vite 构建配置（含 /api 代理到 8080）
│   ├── tailwind.config.js           #   TailwindCSS 配置
│   ├── tsconfig.json                #   TypeScript 配置
│   ├── postcss.config.js            #   PostCSS 配置
│   └── src/
│       ├── main.ts                  #   应用入口
│       ├── App.vue                  #   根组件
│       │
│       ├── router/
│       │   └── index.ts             #   路由配置（含路由守卫）
│       │
│       ├── stores/                  #   Pinia 状态管理
│       │   ├── user.ts              #     用户登录状态
│       │   ├── devices.ts           #     设备列表状态
│       │   └── settings.ts          #     应用设置状态
│       │
│       ├── api/
│       │   └── index.ts             #   后端 API 统一封装
│       │
│       ├── types/
│       │   └── index.ts             #   TypeScript 类型定义
│       │
│       ├── components/              #   公共组件
│       │   ├── Sidebar.vue          #     侧边导航栏（响应式汉堡菜单）
│       │   ├── Header.vue           #     页面头部（搜索 + 通知）
│       │   ├── Card.vue             #     统计卡片
│       │   └── Toast.vue            #     消息提示
│       │
│       ├── views/                   #   页面视图
│       │   ├── Login.vue            #     登录 / 注册页面
│       │   ├── Dashboard.vue        #     仪表盘（统计概览）
│       │   ├── Devices.vue          #     设备管理
│       │   ├── TimeControl.vue      #     时间控制
│       │   ├── BlockList.vue        #     禁止列表
│       │   ├── Statistics.vue       #     使用统计
│       │   └── Settings.vue         #     设置
│       │
│       └── styles/
│           └── tailwind.css         #   TailwindCSS 入口样式
│
└── package-lock.json                # 根目录 lockfile（由 npm 生成）
```

---

## 四、常见问题

**Q: 启动后端报错 `JAVA_HOME is not set`？**
> 安装 JDK 后需要设置环境变量。[教程链接](https://adoptium.net/installation/windows/)

**Q: 前端 `npm install` 很慢？**
> ```bash
> npm config set registry https://registry.npmmirror.com
> npm install
> ```

**Q: 端口被占用？**
> 后端默认 8080，前端默认 5173。如果被占用：
> - 后端：修改 `application.yml` 中的 `server.port`
> - 前端：修改 `vite.config.ts` 中的 `server.port`

**Q: 演示数据在哪？**
> 项目启动时会自动通过 `DataInitializer.java` 注入演示数据，包括：
> - 用户：admin / 123456（监护人）、child / 123456（被保护对象）
> - 设备：小明的手机、客厅电脑、学习平板
> - 时间设置：每日 2 小时，9:00-21:00
> - 禁止列表：抖音、快手、王者荣耀、和平精英、小红书

---

## 五、技术栈总览

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 + TypeScript |
| 构建工具 | Vite 5 |
| UI 样式 | TailwindCSS 3 |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |
| 图表 | Chart.js + vue-chartjs |
| 图标 | Lucide Vue |
| 后端框架 | Spring Boot 3.2 |
| ORM | Spring Data JPA (Hibernate) |
| 安全 | Spring Security + JWT (jjwt 0.12) |
| 数据库 | H2 (开发) / MySQL (生产) |
| 构建 | Maven |
| Java 版本 | 17 |
| 移动端打包 | Capacitor（计划） |
