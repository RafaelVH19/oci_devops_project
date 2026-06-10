$ErrorActionPreference = 'Stop'

mvn clean verify
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven fallo. Corrige errores antes de Docker." -ForegroundColor Red
    exit $LASTEXITCODE
}

# Docker Compose escribe progreso ([+] down/up) en stderr; con Stop eso corta el script en Windows.
$prevEap = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    docker compose -f docker-compose.yml -f docker-compose.dev.yml down --remove-orphans 2>&1 | Out-Null
    docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build 2>&1 | Out-Host
    if ($LASTEXITCODE -ne 0) {
        Write-Host "docker compose fallo (exit $LASTEXITCODE)." -ForegroundColor Red
        exit $LASTEXITCODE
    }
} finally {
    $ErrorActionPreference = $prevEap
}

Write-Host ""
Write-Host "Spring:     http://localhost:8080" -ForegroundColor Green
Write-Host "Auth:       interno (dev: http://localhost:3001)" -ForegroundColor Green
Write-Host ""

# Wait for Spring Boot to be ready before seeding
Write-Host "Esperando que Spring Boot este listo..." -ForegroundColor Yellow
$springReady = $false
for ($i = 1; $i -le 24; $i++) {
    try {
        $null = Invoke-WebRequest -Uri 'http://localhost:8080' -UseBasicParsing -ErrorAction Stop -TimeoutSec 3
        $springReady = $true
        break
    } catch {
        if ($i -lt 24) { Start-Sleep -Seconds 5 }
    }
}

if (-not $springReady) {
    Write-Host "Spring Boot no respondio en 2 minutos. Crea los usuarios manualmente:" -ForegroundColor Red
    Write-Host "  cd auth-server && npm run seed"
} else {
    Write-Host "Spring listo. Creando usuarios de prueba..." -ForegroundColor Yellow
    Push-Location auth-server
    try {
        # Manager
        npm run seed

        # Developer
        $env:SEED_EMAIL    = 'developer@lumen.dev'
        $env:SEED_NAME     = 'Demo Developer'
        $env:SEED_ROLE     = 'DEVELOPER'
        $env:SEED_PASSWORD = 'LumenDev1!'
        npm run seed
    } finally {
        Pop-Location
        Remove-Item Env:\SEED_EMAIL    -ErrorAction SilentlyContinue
        Remove-Item Env:\SEED_NAME     -ErrorAction SilentlyContinue
        Remove-Item Env:\SEED_ROLE     -ErrorAction SilentlyContinue
        Remove-Item Env:\SEED_PASSWORD -ErrorAction SilentlyContinue
    }
    Write-Host ""
    Write-Host "Usuarios listos:" -ForegroundColor Green
    Write-Host "  manager@lumen.dev   / LumenDev1!  (MANAGER)"  -ForegroundColor Cyan
    Write-Host "  developer@lumen.dev / LumenDev1!  (DEVELOPER)" -ForegroundColor Cyan
}
