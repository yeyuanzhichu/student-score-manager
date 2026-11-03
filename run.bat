@echo off
chcp 65001 >nul
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-24"
set "CATALINA_HOME=D:\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55"
set "MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

cd /d "%~dp0"
echo [1/3] 正在打包项目...
call "%MVN%" -q -DskipTests package
if errorlevel 1 (
  echo 打包失败，请检查上方的错误信息。
  pause
  exit /b 1
)

echo [2/3] 部署到 Tomcat...
if not exist "%CATALINA_HOME%\webapps" (
  echo 未找到 Tomcat：%CATALINA_HOME%
  pause
  exit /b 1
)
copy /y "target\student-score-manager.war" "%CATALINA_HOME%\webapps\student-score-manager.war" >nul

echo [3/3] 启动 Tomcat 并打开浏览器...
call "%CATALINA_HOME%\bin\startup.bat"
start "" "http://localhost:8080/student-score-manager/"
echo 完成。若浏览器未自动打开，请访问 http://localhost:8080/student-score-manager/
pause
endlocal
