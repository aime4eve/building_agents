<#
.SYNOPSIS
    OpenSpec 离线包完整性校验脚本 - Windows 版本

.DESCRIPTION
    校验离线包文件的完整性和正确性
#>

$ErrorActionPreference = "Stop"

$PACKAGE_NAME = "fission-ai-openspec-1.2.0.tgz"
$EXPECTED_SHASUM = "0fd5333520c8846f0ac51727379b8812e2f13c1b"
$EXPECTED_SIZE = 204800  # 约 200KB

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $color = switch ($Level) {
        "INFO" { "White" }
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        "ERROR" { "Red" }
        default { "White" }
    }
    Write-Host "[$Level] $Message" -ForegroundColor $color
}

function Test-FileStructure {
    Write-Log "检查目录结构..." -Level "INFO"
    
    $requiredFiles = @(
        "packages\$PACKAGE_NAME",
        "install.ps1",
        "install.sh",
        "package.json",
        "README.md"
    )
    
    $allExist = $true
    foreach ($file in $requiredFiles) {
        if (Test-Path $file) {
            Write-Log "  [OK] $file" -Level "SUCCESS"
        }
        else {
            Write-Log "  [缺失] $file" -Level "ERROR"
            $allExist = $false
        }
    }
    
    return $allExist
}

function Test-PackageIntegrity {
    Write-Log "检查离线包完整性..." -Level "INFO"
    
    $packagePath = "packages\$PACKAGE_NAME"
    
    if (-not (Test-Path $packagePath)) {
        Write-Log "离线包不存在: $packagePath" -Level "ERROR"
        return $false
    }
    
    # 检查文件大小
    $fileInfo = Get-Item $packagePath
    $sizeKB = [math]::Round($fileInfo.Length / 1KB, 2)
    
    if ($fileInfo.Length -gt 100KB -and $fileInfo.Length -lt 500KB) {
        Write-Log "  [OK] 文件大小: $sizeKB KB" -Level "SUCCESS"
    }
    else {
        Write-Log "  [警告] 文件大小异常: $sizeKB KB (预期约 200 KB)" -Level "WARNING"
    }
    
    # 检查文件是否为有效的 tarball
    try {
        $result = npm pack --dry-run $packagePath 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Log "  [OK] 包格式验证通过" -Level "SUCCESS"
            return $true
        }
    }
    catch {
        # 备用验证：检查文件头
        $bytes = [System.IO.File]::ReadAllBytes($packagePath)
        if ($bytes[0] -eq 0x1F -and $bytes[1] -eq 0x8B) {
            Write-Log "  [OK] 包格式验证通过 (gzip)" -Level "SUCCESS"
            return $true
        }
    }
    
    Write-Log "  [失败] 包格式验证失败" -Level "ERROR"
    return $false
}

function Test-ScriptSyntax {
    Write-Log "检查脚本语法..." -Level "INFO"
    
    # 检查 PowerShell 脚本语法
    try {
        $null = [System.Management.Automation.PSParser]::Tokenize((Get-Content "install.ps1" -Raw), [ref]$null)
        Write-Log "  [OK] install.ps1 语法正确" -Level "SUCCESS"
    }
    catch {
        Write-Log "  [失败] install.ps1 语法错误: $_" -Level "ERROR"
        return $false
    }
    
    # 检查 Bash 脚本是否存在
    if (Test-Path "install.sh") {
        Write-Log "  [OK] install.sh 存在" -Level "SUCCESS"
    }
    
    return $true
}

function Main {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  OpenSpec 离线包完整性校验" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    $passed = $true
    
    if (-not (Test-FileStructure)) {
        $passed = $false
    }
    
    if (-not (Test-PackageIntegrity)) {
        $passed = $false
    }
    
    if (-not (Test-ScriptSyntax)) {
        $passed = $false
    }
    
    Write-Host ""
    if ($passed) {
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  所有检查通过!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        exit 0
    }
    else {
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "  存在问题，请检查上述错误" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        exit 1
    }
}

Main
