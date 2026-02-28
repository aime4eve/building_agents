# OpenSpec 离线部署指南

## 概述

本文档提供 OpenSpec 1.2.0 在 Windows 和 Linux 环境中的离线部署方案。适用于无法访问外网（npmjs.com）的隔离网络环境。

## 目录结构

```
npm_offline/
├── packages/
│   └── fission-ai-openspec-1.2.0.tgz   # OpenSpec 离线包
├── install.ps1                          # Windows 安装脚本
├── install.sh                           # Linux 安装脚本
├── package.json                         # npm 配置文件
└── README.md                            # 本文档
```

## 系统要求

| 项目 | 要求 |
|------|------|
| Node.js | >= 20.19.0 |
| npm | 随 Node.js 安装 |
| 操作系统 | Windows 10/11, Linux (Ubuntu 20.04+, CentOS 7+, etc.) |

## 快速开始

### Windows 安装

```powershell
# 方式一：使用 PowerShell 脚本
.\install.ps1              # 本地安装
.\install.ps1 -Global      # 全局安装

# 方式二：手动安装
npm install -g .\packages\fission-ai-openspec-1.2.0.tgz
```

### Linux 安装

```bash
# 添加执行权限
chmod +x install.sh

# 方式一：使用脚本
./install.sh              # 本地安装
./install.sh --global     # 全局安装

# 方式二：手动安装
npm install -g ./packages/fission-ai-openspec-1.2.0.tgz
```

## 详细步骤

### 1. 检查 Node.js 版本

**Windows:**
```powershell
node --version
# 输出应 >= v20.19.0
```

**Linux:**
```bash
node --version
# 输出应 >= v20.19.0
```

如果版本过低，请升级 Node.js：

**Windows (使用 nvm-windows):**
```powershell
nvm install 20.19.0
nvm use 20.19.0
```

**Linux (使用 nvm):**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 20.19.0
nvm use 20.19.0
```

### 2. 执行安装

**Windows:**
```powershell
# 进入离线包目录
cd d:\npm_offline

# 执行安装脚本
.\install.ps1 -Global
```

**Linux:**
```bash
# 进入离线包目录
cd /path/to/npm_offline

# 执行安装脚本
./install.sh --global
```

### 3. 验证安装

```bash
openspec --version
# 输出: 1.2.0
```

## 使用指南

### 初始化项目

```bash
# 进入项目目录
cd your-project

# 初始化 OpenSpec
openspec init
```

初始化过程会：
1. 创建 `openspec/` 目录
2. 询问使用的 AI 工具（Claude Code、Cursor 等）
3. 生成配置文件

### 常用命令

| 命令 | 说明 |
|------|------|
| `openspec init` | 初始化项目 |
| `openspec --version` | 查看版本 |
| `openspec --help` | 查看帮助 |
| `openspec list` | 列出所有规范 |
| `openspec update` | 更新 AI 指令 |

### AI 工具集成

在 AI 工具中使用斜杠命令：

```
/opsx:propose "add dark mode support"
```

这将创建：
- `openspec/changes/add-dark-mode/proposal.md`
- `openspec/changes/add-dark-mode/specs/`
- `openspec/changes/add-dark-mode/design.md`
- `openspec/changes/add-dark-mode/tasks.md`

## 配置选项

### 禁用遥测

OpenSpec 默认收集匿名使用统计。如需禁用：

**Windows:**
```powershell
$env:OPENSPEC_TELEMETRY = "0"
```

**Linux:**
```bash
export OPENSPEC_TELEMETRY=0
```

或添加到配置文件：
```bash
# ~/.bashrc 或 ~/.zshrc
export OPENSPEC_TELEMETRY=0
```

### 配置文件位置

| 平台 | 位置 |
|------|------|
| Windows | `%USERPROFILE%\.openspec\` |
| Linux | `~/.openspec/` |

## 故障排除

### 问题 1: Node.js 版本过低

**错误信息:**
```
error @fission-ai/openspec@1.2.0: The engine "node" is incompatible with this module.
```

**解决方案:**
升级 Node.js 到 20.19.0 或更高版本。

### 问题 2: 权限不足 (Linux)

**错误信息:**
```
EACCES: permission denied
```

**解决方案:**
```bash
# 方式一：使用 sudo
sudo npm install -g ./packages/fission-ai-openspec-1.2.0.tgz

# 方式二：修改 npm 全局目录
mkdir ~/.npm-global
npm config set prefix '~/.npm-global'
echo 'export PATH=~/.npm-global/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### 问题 3: PowerShell 执行策略 (Windows)

**错误信息:**
```
无法加载文件，因为在此系统上禁止运行脚本
```

**解决方案:**
```powershell
# 临时允许
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# 或直接执行
powershell -ExecutionPolicy Bypass -File .\install.ps1
```

### 问题 4: 找不到 openspec 命令

**解决方案:**
```bash
# 检查 npm 全局路径
npm config get prefix

# 添加到 PATH (Linux)
export PATH=$(npm config get prefix)/bin:$PATH

# 添加到 PATH (Windows)
# 将 npm 全局路径添加到系统环境变量
```

## 传输到目标环境

### 方式一：压缩包

**Windows:**
```powershell
Compress-Archive -Path d:\ai-agentic\npm_offline -DestinationPath openspec-offline.zip
```

**Linux:**
```bash
tar -czvf openspec-offline.tar.gz npm_offline/
```

### 方式二：USB 存储

直接复制 `npm_offline` 目录到 USB 存储设备。

## 更新离线包

当需要更新到新版本时：

```bash
# 在有网络的机器上
mkdir npm_offline_new
cd npm_offline_new
mkdir packages
npm pack @fission-ai/openspec@latest

# 复制安装脚本到新目录
# 然后传输到目标环境
```

## 技术支持

- **官方文档**: https://github.com/Fission-AI/OpenSpec
- **问题反馈**: https://github.com/Fission-AI/OpenSpec/issues
- **Discord**: https://discord.gg/YctCnvvshC

## 版本信息

| 项目 | 版本 |
|------|------|
| OpenSpec | 1.2.0 |
| 安装脚本 | 1.0.0 |
| 更新日期 | 2026-02-28 |

---

**注意**: 此离线包仅包含 OpenSpec 主包。如需完整的离线体验，建议在有网络的环境中先运行 `npm install` 下载所有依赖，然后打包整个 `node_modules` 目录。
