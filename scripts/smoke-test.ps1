# mm-security-backend 联调冒烟（start-backend 后执行）
# 用法：从 PowerShell 运行  ./scripts/smoke-test.ps1

$base = "http://localhost:8080"
$ErrorActionPreference = "Stop"

function Call-Rest($method, $path, $body, $headers) {
    $params = @{ Uri = "$base$path"; Method = $method; UseBasicParsing = $true; TimeoutSec = 8 }
    if ($body) { $params.Body = ($body | ConvertTo-Json -Compress); $params.ContentType = "application/json" }
    if ($headers) { $params.Headers = $headers }
    Invoke-RestMethod @params
}

Write-Host "=== 1. /api/v1/health ==="
try {
    $r = Call-Rest GET "/api/v1/health" $null $null
    Write-Host "   > code=$($r.code) service=$($r.data.service) status=$($r.data.status)"
    if ($r.code -ne 0) { throw "health code != 0" }
} catch { Write-Host "   [FAIL] $($_.Exception.Message)"; exit 1 }

Write-Host "=== 2. /api/v1/auth/login (admin / admin@2026) ==="
try {
    $r = Call-Rest POST "/api/v1/auth/login" @{ username = "admin"; password = "admin@2026" } $null
    $token = $r.data.accessToken
    Write-Host "   > code=$($r.code) expiresIn=$($r.data.expiresIn)"
    if ($r.code -ne 0 -or -not $token) { throw "login code != 0" }
} catch { Write-Host "   [FAIL] $($_.Exception.Message)"; exit 1 }

Write-Host "=== 3. /api/v1/dashboard/overview (with token) ==="
try {
    $r = Call-Rest GET "/api/v1/dashboard/overview" $null @{ Authorization = "Bearer $token" }
    Write-Host "   > code=$($r.code) deviceTotal=$($r.data.deviceTotal) alarmToday=$($r.data.alarmToday)"
} catch { Write-Host "   [FAIL] $($_.Exception.Message)"; exit 1 }

Write-Host "=== 4. /api/v1/devices (page, with token) ==="
try {
    $r = Call-Rest GET "/api/v1/devices?page=1&size=5" $null @{ Authorization = "Bearer $token" }
    Write-Host "   > code=$($r.code) total=$($r.data.total) mock=$($r.data.mock)"
} catch { Write-Host "   [FAIL] $($_.Exception.Message)"; exit 1 }

Write-Host "=== 5. wrong pwd expect 401 ==="
try {
    $r = Call-Rest POST "/api/v1/auth/login" @{ username = "admin"; password = "WRONG" } $null
    Write-Host "   > code=$($r.code) msg=$($r.message)"
    if ($r.code -ne 401) { throw "expected 401" }
} catch { Write-Host "   [FAIL] $($_.Exception.Message)"; exit 1 }

Write-Host ""
Write-Host "ALL SMOKE TESTS PASSED"
