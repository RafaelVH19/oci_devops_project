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
Write-Host "Crear usuario de prueba (otra terminal):" -ForegroundColor Yellow
Write-Host "  cd auth-server"
Write-Host "  npm run seed"
Write-Host "  Email: manager@lumen.dev - password: el que imprime seed"
