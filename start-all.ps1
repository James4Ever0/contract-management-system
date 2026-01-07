# 启动前后端服务脚本（在两个独立窗口中启动）
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动合同管理系统" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$scriptPath = $PSScriptRoot

# 启动后端（新窗口）
Write-Host "`n正在启动后端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-File", "$scriptPath\start-backend.ps1"

# 等待2秒后启动前端
Start-Sleep -Seconds 2

# 启动前端（新窗口）
Write-Host "正在启动前端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-File", "$scriptPath\start-frontend.ps1"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  服务启动中..." -ForegroundColor Cyan
Write-Host "  后端地址: http://localhost:8080" -ForegroundColor Yellow
Write-Host "  前端地址: http://localhost:5173" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

