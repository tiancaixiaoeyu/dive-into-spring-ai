param(
  [switch]$CheckOnly
)

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$redisExe = Join-Path $projectRoot ".tools\redis\redis-server.exe"
$mavenCmd = Join-Path $projectRoot ".tools\apache-maven-3.9.6\bin\mvn.cmd"
$frontendDir = Join-Path $projectRoot "front-end"

if (!(Test-Path $redisExe)) { throw "Redis not found: $redisExe" }
if (!(Test-Path $mavenCmd)) { throw "Maven not found: $mavenCmd" }
if (!(Test-Path $frontendDir)) { throw "Frontend dir not found: $frontendDir" }

function Test-PortListening([int]$port) {
  return $null -ne (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1)
}

function Start-WindowProcess([string]$title, [string]$workdir, [string]$command) {
  Start-Process -FilePath "powershell.exe" -ArgumentList @(
    "-NoExit",
    "-Command",
    "`$host.UI.RawUI.WindowTitle='$title'; Set-Location -LiteralPath '$workdir'; $command"
  ) -WorkingDirectory $workdir | Out-Null
}

$redisCmd = "& '$redisExe' --port 6379 --requirepass 123456"
$backendCmd = "& '$mavenCmd' spring-boot:run"
$frontendCmd = "npm run dev"

Write-Host "Project root: $projectRoot"
Write-Host "Redis command: $redisCmd"
Write-Host "Backend command: $backendCmd"
Write-Host "Frontend command: $frontendCmd"

if ($CheckOnly) {
  Write-Host "CheckOnly enabled. No process will be started."
  exit 0
}

if (Test-PortListening 6379) {
  Write-Host "Port 6379 is already listening. Skip Redis."
} else {
  Start-WindowProcess -title "Redis-6379" -workdir $projectRoot -command $redisCmd
  Start-Sleep -Seconds 2
}

if (Test-PortListening 9902) {
  Write-Host "Port 9902 is already listening. Skip backend."
} else {
  Start-WindowProcess -title "Backend-9902" -workdir $projectRoot -command $backendCmd
}

if (Test-PortListening 5177) {
  Write-Host "Port 5177 is already listening. Skip frontend."
} else {
  Start-WindowProcess -title "Frontend-5177" -workdir $frontendDir -command $frontendCmd
}

Write-Host "Startup commands sent. Frontend: http://localhost:5177"
