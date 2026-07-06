@echo off
setlocal

set BASE_URL=https://nomasaccidentes.duckdns.org
set MAILPIT_URL=http://localhost:8025

echo.
echo Ejecutando Selenium EC2 con correos contra %BASE_URL%
echo Este modo necesita que Mailpit de la EC2 sea visible en tu PC en %MAILPIT_URL%.
echo Si no hay tunel SSH a Mailpit, fallaran los casos de correo.
echo.

cd /d "%~dp0Producto\Backend"
mvn clean verify -Pe2e -De2e.base-url=%BASE_URL% -De2e.headless=false -De2e.mailpit-url=%MAILPIT_URL%

echo.
echo Selenium termino. Presiona una tecla para cerrar esta ventana.
pause >nul
