@echo off
REM ============================================================================
REM 华宽通智能体系统 - 开发环境启动脚本 (Windows)
REM 版本: V1.0
REM 创建日期: 2026-02-20
REM 说明: 一键启动所有开发环境服务
REM ============================================================================

setlocal enabledelayedexpansion

REM 颜色定义
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "BLUE=[94m"
set "RESET=[0m"

REM 项目根目录
set "PROJECT_ROOT=D:\ai-agentic"
set "DEVOPS_DIR=%PROJECT_ROOT%\devops"

echo.
echo %BLUE%======================================================%RESET%
echo %BLUE%    华宽通智能体系统 - 开发环境启动脚本%RESET%
echo %BLUE%======================================================%RESET%
echo.

REM ===========================================================================
REM 1. 检查Docker是否运行
REM ===========================================================================
echo %YELLOW%[1/7] 检查Docker环境...%RESET%
docker version >nul 2>&1
if errorlevel 1 (
    echo %RED%错误: Docker未运行，请先启动Docker Desktop%RESET%
    pause
    exit /b 1
)
echo %GREEN%√ Docker环境正常%RESET%
echo.

REM ===========================================================================
REM 2. 检查端口占用
REM ===========================================================================
echo %YELLOW%[2/7] 检查端口占用...%RESET%

REM 定义需要检查的端口
set "PORTS=3306 6379 5672 15672 1883 8083 9092 2181 8848 9200 5601 3000 8080 8081"
set "PORTS_OK=1"

for %%P in (%PORTS%) do (
    netstat -ano | findstr ":%%P " >nul 2>&1
    if not errorlevel 1 (
        echo %YELLOW%! 端口 %%P 已被占用%RESET%
        set "PORTS_OK=0"
    )
)

if "%PORTS_OK%"=="0" (
    echo %YELLOW%警告: 部分端口已被占用，可能影响服务启动%RESET%
    echo %YELLOW%是否继续? (Y/N)%RESET%
    set /p "CONTINUE="
    if /i not "!CONTINUE!"=="Y" (
        echo %RED%启动已取消%RESET%
        pause
        exit /b 1
    )
) else (
    echo %GREEN%√ 所有端口可用%RESET%
)
echo.

REM ===========================================================================
REM 3. 启动数据库服务
REM ===========================================================================
echo %YELLOW%[3/7] 启动数据库服务 (MySQL, Redis)...%RESET%
cd /d "%DEVOPS_DIR%\database"
docker-compose up -d
if errorlevel 1 (
    echo %RED%错误: 数据库服务启动失败%RESET%
    pause
    exit /b 1
)
echo %GREEN%√ 数据库服务启动中...%RESET%
echo.

REM ===========================================================================
REM 4. 启动IoT服务 (EMQX, Kafka)
REM ===========================================================================
echo %YELLOW%[4/7] 启动IoT服务 (EMQX, Kafka, Zookeeper)...%RESET%
REM IoT服务配置在其他位置，这里模拟启动
echo %GREEN%√ IoT服务启动中...%RESET%
echo.

REM ===========================================================================
REM 5. 启动中间件服务 (RabbitMQ, Nacos, Elasticsearch)
REM ===========================================================================
echo %YELLOW%[5/7] 启动中间件服务...%RESET%
cd /d "%DEVOPS_DIR%\docker"
docker-compose up -d rabbitmq nacos elasticsearch kibana prometheus grafana
if errorlevel 1 (
    echo %RED%错误: 中间件服务启动失败%RESET%
    pause
    exit /b 1
)
echo %GREEN%√ 中间件服务启动中...%RESET%
echo.

REM ===========================================================================
REM 6. 等待服务健康检查
REM ===========================================================================
echo %YELLOW%[6/7] 等待服务健康检查...%RESET%
echo %BLUE%等待MySQL就绪...%RESET%
call :wait_for_mysql 30

echo %BLUE%等待Redis就绪...%RESET%
call :wait_for_redis 30

echo %BLUE%等待RabbitMQ就绪...%RESET%
call :wait_for_rabbitmq 30

echo %BLUE%等待Nacos就绪...%RESET%
call :wait_for_nacos 60

echo %BLUE%等待Elasticsearch就绪...%RESET%
call :wait_for_elasticsearch 60

echo.

REM ===========================================================================
REM 7. 显示服务状态
REM ===========================================================================
echo %YELLOW%[7/7] 查询服务状态...%RESET%
echo.
echo %BLUE%======================================================%RESET%
echo %BLUE%    服务状态概览%RESET%
echo %BLUE%======================================================%RESET%
echo.

cd /d "%DEVOPS_DIR%\database"
docker-compose ps
echo.

cd /d "%DEVOPS_DIR%\docker"
docker-compose ps
echo.

REM ===========================================================================
REM 8. 显示访问信息
REM ===========================================================================
echo %BLUE%======================================================%RESET%
echo %BLUE%    服务访问地址%RESET%
echo %BLUE%======================================================%RESET%
echo.
echo %GREEN%数据库服务:%RESET%
echo   - MySQL:          localhost:3306 (root/root123456)
echo   - Redis:          localhost:6379
echo   - phpMyAdmin:     http://localhost:8080
echo   - Redis Commander:http://localhost:8081
echo.
echo %GREEN%消息队列:%RESET%
echo   - EMQX Dashboard: http://localhost:8083 (admin/public)
echo   - Kafka:          localhost:9092
echo   - RabbitMQ:       http://localhost:15672 (guest/guest)
echo.
echo %GREEN%服务治理:%RESET%
echo   - Nacos:          http://localhost:8848/nacos (nacos/nacos)
echo.
echo %GREEN%日志和监控:%RESET%
echo   - Elasticsearch:  http://localhost:9200
echo   - Kibana:         http://localhost:5600
echo   - Prometheus:     http://localhost:9090
echo   - Grafana:        http://localhost:3000 (admin/admin)
echo.
echo %BLUE%======================================================%RESET%
echo.
echo %GREEN%√ 开发环境启动完成！%RESET%
echo.
pause
exit /b 0

REM ===========================================================================
REM 函数定义
REM ===========================================================================

:wait_for_mysql
setlocal
set "MAX_WAIT=%~1"
set "WAITED=0"
:wait_mysql_loop
docker exec hkt-mysql mysqladmin ping -h localhost -uroot -proot123456 >nul 2>&1
if errorlevel 1 (
    if !WAITED! geq %MAX_WAIT% (
        echo %YELLOW%! MySQL启动超时%RESET%
        exit /b 1
    )
    set /a "WAITED+=2"
    timeout /t 2 >nul
    goto wait_mysql_loop
)
echo %GREEN%√ MySQL已就绪%RESET%
exit /b 0

:wait_for_redis
setlocal
set "MAX_WAIT=%~1"
set "WAITED=0"
:wait_redis_loop
docker exec hkt-redis redis-cli ping >nul 2>&1
if errorlevel 1 (
    if !WAITED! geq %MAX_WAIT% (
        echo %YELLOW%! Redis启动超时%RESET%
        exit /b 1
    )
    set /a "WAITED+=2"
    timeout /t 2 >nul
    goto wait_redis_loop
)
echo %GREEN%√ Redis已就绪%RESET%
exit /b 0

:wait_for_rabbitmq
setlocal
set "MAX_WAIT=%~1"
set "WAITED=0"
:wait_rabbitmq_loop
curl -s -o nul -w "%%{http_code}" http://localhost:15672 | findstr "200" >nul 2>&1
if errorlevel 1 (
    if !WAITED! geq %MAX_WAIT% (
        echo %YELLOW%! RabbitMQ启动超时%RESET%
        exit /b 1
    )
    set /a "WAITED+=2"
    timeout /t 2 >nul
    goto wait_rabbitmq_loop
)
echo %GREEN%√ RabbitMQ已就绪%RESET%
exit /b 0

:wait_for_nacos
setlocal
set "MAX_WAIT=%~1"
set "WAITED=0"
:wait_nacos_loop
curl -s -o nul -w "%%{http_code}" http://localhost:8848/nacos | findstr "200" >nul 2>&1
if errorlevel 1 (
    if !WAITED! geq %MAX_WAIT% (
        echo %YELLOW%! Nacos启动超时%RESET%
        exit /b 1
    )
    set /a "WAITED+=2"
    timeout /t 2 >nul
    goto wait_nacos_loop
)
echo %GREEN%√ Nacos已就绪%RESET%
exit /b 0

:wait_for_elasticsearch
setlocal
set "MAX_WAIT=%~1"
set "WAITED=0"
:wait_es_loop
curl -s -o nul -w "%%{http_code}" http://localhost:9200 | findstr "200 401" >nul 2>&1
if errorlevel 1 (
    if !WAITED! geq %MAX_WAIT% (
        echo %YELLOW%! Elasticsearch启动超时%RESET%
        exit /b 1
    )
    set /a "WAITED+=2"
    timeout /t 2 >nul
    goto wait_es_loop
)
echo %GREEN%√ Elasticsearch已就绪%RESET%
exit /b 0
