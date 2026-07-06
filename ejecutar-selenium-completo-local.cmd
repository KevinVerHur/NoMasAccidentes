@echo off
setlocal

set BASE_URL=http://127.0.0.1:5173
set BACKEND_URL=http://127.0.0.1:8080
set MAILPIT_URL=http://127.0.0.1:8025

echo.
echo Ejecutando Selenium COMPLETO local contra %BASE_URL%
echo Este modo usa Mailpit en %MAILPIT_URL% y ejecuta tambien los casos de correo.
echo.
echo Levantando ambiente E2E local con Docker: MySQL, Mailpit, backend y frontend.
echo.

echo Limpiando base de datos E2E anterior...
docker compose -f "%~dp0docker-compose.e2e.yml" down -v --remove-orphans
if errorlevel 1 (
  echo.
  echo ERROR: No pude limpiar el ambiente E2E anterior.
  echo Revisa que Docker Desktop este abierto y vuelve a intentar.
  echo.
  exit /b 1
)

docker compose -f "%~dp0docker-compose.e2e.yml" up -d --build
if errorlevel 1 (
  echo.
  echo ERROR: No pude levantar el ambiente E2E con Docker.
  echo Revisa que Docker Desktop este abierto y vuelve a intentar.
  echo.
  exit /b 1
)

echo Esperando servicios E2E...
timeout /t 10 /nobreak >nul

echo Verificando frontend local...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r = Invoke-WebRequest -Uri '%BASE_URL%' -UseBasicParsing -TimeoutSec 8; if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { exit 0 } else { exit 1 } } catch { exit 1 }"
if errorlevel 1 (
  echo.
  echo ERROR: No pude abrir %BASE_URL%.
  echo Abre otra terminal, levanta el frontend con npm run dev y vuelve a intentar.
  echo.
  exit /b 1
)

echo Verificando backend local...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ok=$false; for($i=0;$i -lt 30;$i++){ try { $r = Invoke-WebRequest -Uri '%BACKEND_URL%/api/auth/reset-password/validate?token=selenium-check' -UseBasicParsing -TimeoutSec 5; if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { $ok=$true; break } } catch {}; Start-Sleep -Seconds 2 }; if($ok){exit 0}else{exit 1}"
if errorlevel 1 (
  echo.
  echo ERROR: No pude abrir el backend local en %BACKEND_URL%.
  echo Levanta el backend local y vuelve a intentar.
  echo.
  exit /b 1
)

echo Verificando Mailpit local...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r = Invoke-WebRequest -Uri '%MAILPIT_URL%/api/v1/messages' -UseBasicParsing -TimeoutSec 8; if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { exit 0 } else { exit 1 } } catch { exit 1 }"
if errorlevel 1 (
  echo.
  echo ERROR: No pude abrir Mailpit en %MAILPIT_URL%.
  echo Levanta Mailpit local y vuelve a intentar.
  echo.
  exit /b 1
)

cd /d "%~dp0Producto\Backend"
mvn clean verify -Pe2e -De2e.base-url=%BASE_URL% -De2e.headless=false -De2e.mailpit-url=%MAILPIT_URL%
set TEST_EXIT=%ERRORLEVEL%

echo.
echo Limpiando artefactos temporales de target y conservando reportes/capturas...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$target='target'; $keep=@('failsafe-reports','surefire-reports','selenium-screenshots'); if(Test-Path $target){ Get-ChildItem $target -Force | Where-Object { $keep -notcontains $_.Name } | Remove-Item -Recurse -Force }"

echo.
echo Selenium termino.
exit /b %TEST_EXIT%
