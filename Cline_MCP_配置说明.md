# Cline MCP 配置说明

## 当前状态

您遇到的最初错误是关于 `cline` 包的安装问题，但这个错误实际上不会影响您的合同管理系统项目的运行。

## 合同管理系统状态

✅ 后端服务已在 8080 端口运行  
✅ 前端开发服务已在 5173 端口运行  
✅ 系统可以正常使用

## ABAP ADT MCP 服务器配置

如果您需要使用 SAP ABAP 开发工具集成，请按照以下步骤操作：

### 1. 安装 ABAP ADT MCP 服务器

```bash
npm install -g @mario-andreschak/mcp-abap-adt
```

### 2. 配置 SAP 连接信息

配置文件位置：
`C:\Users\Administrator\AppData\Roaming\Code\User\globalStorage\saoudrizwan.claude-dev\settings\cline_mcp_settings.json`

当前配置模板：
```json
{
  "mcpServers": {
    "abap-adt": {
      "command": "node",
      "args": [
        "C:\\Users\\Administrator\\AppData\\Roaming\\npm\\node_modules\\@mario-andreschak\\mcp-abap-adt\\dist\\index.js"
      ],
      "env": {
        "ABAP_URL": "http://your-sap-system:8000",
        "ABAP_CLIENT": "001",
        "ABAP_USER": "your-username",
        "ABAP_PASSWORD": "your-password",
        "ABAP_LANGUAGE": "EN"
      }
    }
  }
}
```

### 3. 需要配置的参数

- **ABAP_URL**: SAP 系统的 URL（例如：http://sap-server.example.com:8000）
- **ABAP_CLIENT**: SAP 客户端编号（通常是 001, 100, 200 等）
- **ABAP_USER**: SAP 用户名
- **ABAP_PASSWORD**: SAP 用户密码
- **ABAP_LANGUAGE**: 语言代码（EN=英语, ZH=中文, JA=日语等）

### 4. 配置步骤

1. 获取您的 SAP 系统连接信息
2. 编辑配置文件，替换占位符信息
3. 重启 Visual Studio Code
4. MCP 服务器将自动连接到您的 SAP 系统

### 5. 注意事项

- 确保您的 SAP 系统已启用 ABAP Development Tools (ADT) 服务
- 确保您的 SAP 用户有足够的权限访问 ADT 服务
- 密码将以明文形式存储在配置文件中，请注意安全

## 如果不需要 SAP 集成

如果您只是想运行合同管理系统，可以忽略上述配置。系统已经可以正常使用了：

- 访问前端：http://localhost:5173
- 后端 API：http://localhost:8080

## 原始错误说明

最初的错误 `npm WARN EBADENGINE` 是因为尝试安装 `cline` 包，该包：
- 不支持 Windows 平台（只支持 darwin/linux）
- 需要 Node.js >= 20.0.0（您当前使用 v18.16.0）

这个错误与您的合同管理系统项目无关，不会影响项目的正常运行。