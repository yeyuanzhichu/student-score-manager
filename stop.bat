@echo off
chcp 65001 >nul
setlocal
set "CATALINA_HOME=D:\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55"
call "%CATALINA_HOME%\bin\shutdown.bat"
echo 已发送停止指令。
pause
endlocal
