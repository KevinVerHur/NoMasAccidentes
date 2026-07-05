@echo off
setlocal

set BASE_URL=https://nomasaccidentes.duckdns.org

echo.
echo Ejecutando Selenium visible contra %BASE_URL%
echo Se abrira Chrome y se rellenaran los formularios automaticamente.
echo.

cd /d "%~dp0Producto\Backend"
mvn clean verify -Pe2e -De2e.base-url=%BASE_URL% -De2e.headless=false

echo.
echo Selenium termino. Presiona una tecla para cerrar esta ventana.
pause >nul
