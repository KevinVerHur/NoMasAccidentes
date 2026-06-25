# NoMasAccidentes

Proyecto con backend Spring Boot, frontend React/Vite y base de datos MySQL. Esta guia describe el entorno Docker para desarrollo local sin cambiar la forma tradicional de ejecutar cada modulo fuera de Docker.

## Requisitos Previos

- Docker Desktop o Docker Engine con Docker Compose.
- Puertos locales disponibles:
  - `3306` para MySQL.
  - `8080` para el backend.
  - `5173` para el frontend.

## Levantar El Entorno

Desde la raiz del repositorio:

```bash
docker compose up --build
```

Docker Compose levantara los servicios:

- `mysql`: base de datos MySQL con volumen persistente.
- `backend`: aplicacion Spring Boot en perfil `dev`.
- `frontend`: aplicacion React/Vite.

El frontend queda disponible en:

```text
http://localhost:5173
```

El backend queda disponible en:

```text
http://localhost:8080
```

Swagger UI queda disponible en:

```text
http://localhost:8080/swagger-ui.html
```

## Variables De Entorno

El archivo `.env.example` contiene las variables necesarias para personalizar el entorno local. Si necesitas cambiar puertos, credenciales locales o configuracion de correo, copia el archivo:

```bash
cp .env.example .env
```

En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

No subas credenciales reales al repositorio.

## Puertos Usados

| Servicio | Puerto local | Puerto contenedor |
| --- | ---: | ---: |
| MySQL | `3306` | `3306` |
| Backend | `8080` | `8080` |
| Frontend | `5173` | `5173` |

Si algun puerto ya esta ocupado, ajusta `MYSQL_PORT`, `BACKEND_PORT` o `FRONTEND_PORT` en `.env`.

## Base De Datos

El backend dentro de Docker se conecta a MySQL usando el nombre del servicio:

```text
mysql
```

La URL se inyecta desde `docker-compose.yml` mediante `SPRING_DATASOURCE_URL`, por lo que no es necesario modificar `application-dev.properties`. Fuera de Docker, el proyecto puede seguir usando la configuracion local existente con `localhost:3306`.

Para conectarte desde una herramienta como MySQL Workbench, DBeaver o DataGrip:

```text
Host: localhost
Port: valor de MYSQL_PORT en .env (por ejemplo 3306, o 3307 si el 3306 esta ocupado)
Database: no_mas_accidentes
User: nma_user
Password: valor definido en DB_PASS
```

Si no creas `.env`, Docker Compose usara valores locales por defecto no pensados para produccion.

## Detener Contenedores

Para detener los servicios sin borrar datos:

```bash
docker compose down
```

Para detener y borrar tambien los volumenes locales, incluida la base de datos:

```bash
docker compose down -v
```

## Reconstruir Imagenes

Si cambian dependencias o Dockerfiles:

```bash
docker compose build --no-cache
docker compose up
```

Tambien puedes reconstruir y levantar en un solo paso:

```bash
docker compose up --build
```

## Comandos Utiles

Ver logs de todos los servicios:

```bash
docker compose logs -f
```

Ver logs solo del backend:

```bash
docker compose logs -f backend
```

Ver logs solo del frontend:

```bash
docker compose logs -f frontend
```

Entrar al cliente MySQL dentro del contenedor:

```bash
docker compose exec mysql mysql -u nma_user -p no_mas_accidentes
```

## Problemas Comunes

Si MySQL no inicia porque el puerto `3306` esta ocupado, cambia `MYSQL_PORT` en `.env` o detiene tu MySQL local.

Si el backend falla al validar tablas, revisa los logs de Flyway:

```bash
docker compose logs -f backend
```

Si el frontend no logra llamar al backend, revisa que `VITE_API_URL` apunte a:

```text
http://localhost:8080
```

Si necesitas probar correos en Docker, define `MAIL_USER` y `MAIL_PASS` en `.env`. La configuracion Docker sobreescribe las credenciales directas del perfil `dev` mediante variables `SPRING_MAIL_USERNAME` y `SPRING_MAIL_PASSWORD`.

Si cambiaste variables usadas por Vite, reconstruye el frontend:

```bash
docker compose up --build frontend
```

## Notas Para Futuro Despliegue En AWS

Esta configuracion esta orientada a desarrollo local. Para AWS conviene preparar una configuracion separada usando variables de entorno reales, un servicio administrado para MySQL como RDS, almacenamiento de PDFs en S3, secretos fuera del repositorio y un pipeline de despliegue definido en una etapa posterior.

No se incluye Kubernetes, Terraform, ECS, EKS, GitHub Actions ni configuracion avanzada de produccion en esta fase.
