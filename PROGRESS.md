# 开发进度：已完成

> 本项目已开发完成并通过端到端测试（教师登录、成绩录入/清除、学生查询、
> 平均分/及格率统计、学生/课程增删、CSV 导出、角色权限隔离）。

## 当前状态

- 项目位置：`C:\Users\缘\Desktop\zy\项目\student-score-manager`
- 已构建产物：`target\student-score-manager.war`（约 6.1 MB）
- 运行与使用说明：见 [README.md](README.md)
- 一键启动：双击 `run.bat`；一键停止：双击 `stop.bat`

## 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 教师 | `teacher` | `123456` |
| 学生 | `2024010101` | `123456` |
| 学生 | `2024010102` | `123456` |

首次启动自动生成 3 个班级、30 名学生、5 门课程、117 条成绩。
数据库文件：`C:\Users\缘\.student-score-manager\grades.mv.db`
