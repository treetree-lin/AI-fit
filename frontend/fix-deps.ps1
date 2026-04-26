# 修复 frontend 依赖
Write-Host "修复 frontend 依赖..." -ForegroundColor Yellow

# 1. 读取 package.json
$packageJson = Get-Content package.json -Raw | ConvertFrom-Json

# 2. 替换所有的 workspace:* 为 latest
function Fix-Dependencies {
    param($deps)
    if ($deps) {
        $deps.PSObject.Properties | ForEach-Object {
            if ($_.Value -eq "workspace:*") {
                Write-Host "  修复: $($_.Name) from workspace:* to latest" -ForegroundColor Yellow
                $_.Value = "latest"
            }
        }
    }
}

# 修复 dependencies
Fix-Dependencies $packageJson.dependencies

# 修复 devDependencies
Fix-Dependencies $packageJson.devDependencies

# 3. 保存
$packageJson | ConvertTo-Json -Depth 10 | Set-Content package.json.fixed
Move-Item package.json.fixed package.json -Force

Write-Host " package.json 已修复" -ForegroundColor Green
