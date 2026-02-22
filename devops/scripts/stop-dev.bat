@echo off
REM ============================================================================
REM 华宽通智能体系统 - 开发环境停止脚本 (Windows)
REM 版本: V1.0
REM 创建日期: 2026-02-20
REM 说明: 停止所有开发环境服务
REM ============================================================================

setlocal enabledelayedexpansion

set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "BLUE=[94m"
set "RESET=[0m"

set "PROJECT_ROOT=D:\ai-agentic"
set "DEVOPS_DIR=%PROJECT_ROOT%\devops"

echo.
echo %BLUE%======================================================%RESET%
echo %BLUE%    华宽通智能体系统 - 开发环境停止脚本%RESET%
echo %BLUE%======================================================%RESET%
echo.

echo %YELLOW%请选择停止方式:%RESET%
echo   1. 停止所有服务 (保留数据)
echo   2. 停止并删除容器 (保留数据)
echo   3. 完全清理 (包括数据)
echo.
set /p "CHOICE=请输入选项 (1-3): "

if "%CHOICE%"=="1" goto STOP_ONLY
if "%CHOICE%"=="2" goto STOP_REMOVE
if "%CHOICE%"=="3" goto CLEAN_ALL
echo %RED%无效选项%RESET%
pause
exit /b 1

:STOP_ONLY
echo.
echo %YELLOW%停止所有服务...%RESET%
cd /d "%DEVOPS_DIR%\database"
docker-compose stop
cd /d "%DEVOPS_DIR%\docker"
docker-compose stop
echo %GREEN%√ 所有服务已停止%RESET%
pause
exit /b 0

:STOP_REMOVE
echo.
echo %YELLOW%停止并删除容器...%RESET%
cd /d "%DEVOPS_DIR%\database"
docker-compose down
cd /d "%DEVOPS_DIR%\docker"
docker-compose down
echo %GREEN%√ 容器已删除，数据保留%RESET%
pause
exit /b 0

:CLEAN_ALL
echo.
echo %RED%警告: 此操作将删除所有数据卷!%RESET%
set /p "CONFIRM=确认执行? (yes/no): "
if /i not "%CONFIRM%"=="yes" (
    echo %YELLOW%操作已取消%RESET%
    pause
    exit /b 0
)

echo.
echo %YELLOW%完全清理所有服务和数据...%RESET%
cd /d "%DEVOPS_DIR%\database"
docker-compose down -v
cd /d "%DEVOPS_DIR%\docker"
docker-compose down -v
echo %GREEN%√ 完全清理完成%RESET%
pause
exit /b 0
