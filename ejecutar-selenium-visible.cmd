@echo off
setlocal

set BASE_URL=https://nomasaccidentes.duckdns.org

echo.
echo Ejecutando Selenium visible contra %BASE_URL%
echo Se abrira Chrome y se rellenaran los formularios automaticamente.
echo Modo EC2: los casos que dependen de Mailpit se omiten si no hay tunel de correo.
echo.

cd /d "%~dp0Producto\Backend"
mvn clean verify -Pe2e -De2e.base-url=%BASE_URL% -De2e.headless=false -De2e.mailpit-url=disabled

echo.
echo Selenium termino. Presiona una tecla para cerrar esta ventana.
pause >nul
