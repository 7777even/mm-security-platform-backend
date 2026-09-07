@echo off
chcp 65001 >nul
echo ============================================================
echo  mm-security-backend 启动脚本（Windows dev 环境）
echo  Profile: dev  （H2 兜底，无需 PostgreSQL）
echo  Port:     8080
echo  Health:   http://localhost:8080/api/v1/health
echo  Default:  admin / admin@2026
echo ============================================================

setlocal
cd /d "%~dp0.."

where mvnw >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 未在项目根目录发现 mvnw，请确认执行路径。
    exit /b 1
)

echo [INFO] 正在启动 Spring Boot（首次启动会下载依赖，约 1～3 分钟）...
call mvnw spring-boot:run

endlocal
