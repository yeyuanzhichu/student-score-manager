# 班级学生成绩管理系统

一个基于 **Java EE（Jakarta Servlet 6 + JSP + JSTL + JDBC + H2）** 的网页成绩管理系统，
用于课程设计，覆盖需求：

- 教师录入 / 修改成绩
- 学生查询个人成绩
- 平均分、及格率、优秀率统计
- 班级 / 课程维度的成绩分析与排名
- 学生与课程的基础档案管理

## 技术栈

| 层次 | 技术 |
|---|---|
| 前端 | JSP + JSTL 3 + 原生 CSS / JavaScript（无外部 CDN 依赖） |
| 后端 | Jakarta Servlet 6、JSP 3.1（Tomcat 10.1） |
| 数据访问 | JDBC，全部使用 PreparedStatement 防止 SQL 注入 |
| 数据库 | H2 文件数据库，首次启动自动建表并生成演示数据 |
| 安全 | BCrypt 密码哈希、Session 登录、角色过滤器 |

## 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 教师 | `teacher` | `123456` |
| 学生 | `2024010101` | `123456` |

首次启动会生成 3 个班级、30 名学生、5 门课程和约 120 条成绩记录。

## 在 IntelliJ IDEA 中运行

1. 用 IDEA 打开本项目目录（`File → Open` 选择 `pom.xml` 所在目录）。
2. 等待 Maven 依赖自动下载完成（首次需联网）。
3. 配置一个本地 Tomcat 10.1 运行配置：
   - `Run → Edit Configurations → + → Tomcat Server → Local`
   - `Application server` 选择本机 `D:\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55`
   - `Deployment` 添加 `student-score-manager:war exploded`，`Application context` 填 `/student-score-manager`
   - `Before launch` 保留 `Build artifact`
4. 点击运行，浏览器访问：`http://localhost:8080/student-score-manager/`

## 手动打包与部署（命令行）

```powershell
# 1. 打包（生成的 WAR 在 target/student-score-manager.war）
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd" -f "pom.xml" clean package

# 2. 部署到本机 Tomcat
Copy-Item "target\student-score-manager.war" "D:\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55\webapps\"

# 3. 启动 Tomcat
$env:CATALINA_HOME="D:\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55"
& "$env:CATALINA_HOME\bin\startup.bat"
```

## 功能说明

- **登录鉴权**：教师与学生在同一个登录框输入账号，系统按账号类型自动区分角色。
- **教师工作台**：学生数、课程数、成绩数、平均分、及格率总览。
- **成绩录入**：选择班级 + 课程，按花名册批量录入，实时校验 0～100 分。
- **学生管理**：新增 / 编辑 / 删除学生，学生以学号登录。
- **课程管理**：课程、学分、任课教师维护。
- **统计分析**：班级平均分、及格率、优秀率、分数段分布、学生排名，支持 CSV 导出。
- **学生查询**：个人成绩单、班级平均分对比、班级排名、已获学分、打印成绩单。

## 数据库位置

默认：`C:\Users\你的用户名\.student-score-manager\grades.mv.db`

如需自定义，可在启动参数或环境变量中指定：

```powershell
$env:SCORE_DB_PATH="D:\data\grades"
```

## 目录结构

```
src/main/java/com/example/score
├── config        # H2 连接与数据初始化
├── dao           # 数据访问层
├── model         # 实体与视图模型
├── service       # 业务逻辑
├── util          # 工具类
└── web           # Servlet 控制器与过滤器
src/main/resources/db/schema.sql
src/main/webapp
├── WEB-INF/web.xml
├── WEB-INF/views  # JSP 视图
└── assets         # CSS / JS / 图标
```
