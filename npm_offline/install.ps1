<#
.SYNOPSIS
    OpenSpec 离线安装脚本 - Windows 版本

.DESCRIPTION
    此脚本用于在无法访问外网的 Windows 环境中安装 OpenSpec。
    需要 Node.js 20.19.0 或更高版本。

.PARAMETER InstallPath
    安装路径，默认为当前目录

.PARAMETER Global
    是否全局安装

.EXAMPLE
    .\install.ps1
    .\install.ps1 -Global
    .\install.ps1 -InstallPath "C:\Tools\openspec"
#>

param(
    [string]$InstallPath = $PWD,
    [switch]$Global = $false
)

$ErrorActionPreference = "Stop"
$RequiredNodeVersion = "20.19.0"
$PackageName = "fission-ai-openspec-1.2.0.tgz"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "INFO" { "White" }
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        "ERROR" { "Red" }
        default { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Test-NodeVersion {
    try {
        $nodeVersion = (node --version) -replace 'v', ''
        $currentVersion = [version]$nodeVersion
        $requiredVersion = [version]$RequiredNodeVersion
        
        if ($currentVersion -lt $requiredVersion) {
            Write-Log "Node.js 版本过低: v$nodeVersion, 需要版本: v$RequiredNodeVersion 或更高" -Level "ERROR"
            Write-Log "请使用 nvm-windows 安装 Node.js:" -Level "INFO"
            Write-Log "  nvm install $RequiredNodeVersion" -Level "INFO"
            Write-Log "  nvm use $RequiredNodeVersion" -Level "INFO"
            return $false
        }
        
        Write-Log "Node.js 版本检查通过: v$nodeVersion" -Level "SUCCESS"
        return $true
    }
    catch {
        Write-Log "未检测到 Node.js，请先安装 Node.js $RequiredNodeVersion 或更高版本" -Level "ERROR"
        Write-Log "下载地址: https://nodejs.org/" -Level "INFO"
        return $false
    }
}

function Test-PackageExists {
    $packagePath = Join-Path $InstallPath "packages\$PackageName"
    if (-not (Test-Path $packagePath)) {
        Write-Log "离线包不存在: $packagePath" -Level "ERROR"
        return $false
    }
    Write-Log "离线包检查通过: $packagePath" -Level "SUCCESS"
    return $true
}

function Install-OpenSpec {
    param([bool]$IsGlobal)
    
    $packagePath = Join-Path $InstallPath "packages\$PackageName"
    
    if ($IsGlobal) {
        Write-Log "正在全局安装 OpenSpec..." -Level "INFO"
        npm install -g $packagePath
    }
    else {
        Write-Log "正在本地安装 OpenSpec..." -Level "INFO"
        npm install $packagePath --save-dev
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Log "安装失败" -Level "ERROR"
        return $false
    }
    
    return $true
}

function Test-Installation {
    Write-Log "验证安装..." -Level "INFO"
    
    try {
        $version = npx openspec --version 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Log "OpenSpec 安装成功! 版本: $version" -Level "SUCCESS"
            return $true
        }
    }
    catch {
        # 尝试直接调用
        $version = openspec --version 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Log "OpenSpec 安装成功! 版本: $version" -Level "SUCCESS"
            return $true
        }
    }
    
    Write-Log "安装验证失败" -Level "ERROR"
    return $false
}

function Show-Usage {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  OpenSpec 使用指南" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "初始化项目:" -ForegroundColor Yellow
    Write-Host "  cd your-project" -ForegroundColor White
    Write-Host "  npx openspec init" -ForegroundColor White
    Write-Host ""
    Write-Host "创建变更提案:" -ForegroundColor Yellow
    Write-Host "  /opsx:propose `"your feature description`"" -ForegroundColor White
    Write-Host ""
    Write-Host "查看帮助:" -ForegroundColor Yellow
    Write-Host "  openspec --help" -ForegroundColor White
    Write-Host ""
    Write-Host "禁用遥测:" -ForegroundColor Yellow
    Write-Host "  `$env:OPENSPEC_TELEMETRY = '0'" -ForegroundColor White
    Write-Host ""
}

function Main {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  OpenSpec 离线安装程序 v1.0.0" -ForegroundColor Cyan
    Write-Host "  Windows 版本" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Log "开始安装..." -Level "INFO"
    Write-Log "安装路径: $InstallPath" -Level "INFO"
    Write-Log "安装模式: $(if ($Global) { '全局' } else { '本地' })" -Level "INFO"
    
    if (-not (Test-NodeVersion)) {
        exit 1
    }
    
    if (-not (Test-PackageExists)) {
        exit 1
    }
    
    if (-not (Install-OpenSpec -IsGlobal $Global)) {
        exit 1
    }
    
    if (-not (Test-Installation)) {
        exit 1
    }
    
    Show-Usage
    
    Write-Log "安装完成!" -Level "SUCCESS"
}

Main
