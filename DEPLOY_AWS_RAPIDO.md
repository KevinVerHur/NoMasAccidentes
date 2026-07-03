# Deploy AWS Rapido - NoMasAccidentes

Guia practica para desplegar NoMasAccidentes en AWS sin guardar secretos en el repositorio.

## 1. Preparar AWS CLI local

Instala AWS CLI v2 y configura un perfil con permisos suficientes:

```bash
aws configure
aws sts get-caller-identity
```

No pegues credenciales en archivos del proyecto. Usa perfiles locales, variables seguras, IAM Roles u OIDC.

## 2. Crear RDS MySQL

Parametros minimos sugeridos:

- Motor: MySQL 8.x.
- DB name: `no_mas_accidentes`.
- Puerto: `3306`.
- Usuario admin: definir en AWS, no guardar en Git.
- Acceso publico: preferentemente `No`.
- Security Group: permitir entrada `3306` solo desde el Security Group de Elastic Beanstalk.
- Backups: habilitar al menos retencion corta para produccion.

URL JDBC esperada para Elastic Beanstalk:

```text
jdbc:mysql://HOST_RDS:3306/no_mas_accidentes?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
```

## 3. Crear Elastic Beanstalk Backend

Crear aplicacion y environment con plataforma Java 21 / Corretto si esta disponible.

Variables de entorno obligatorias en Elastic Beanstalk:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://HOST_RDS:3306/no_mas_accidentes?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
DB_USER=REEMPLAZAR_USUARIO_RDS
DB_PASS=REEMPLAZAR_PASSWORD_RDS
JWT_SECRET=REEMPLAZAR_JWT_SECRET_LARGO_Y_SEGURO
CORS_ALLOWED_ORIGINS=https://URL_CLOUDFRONT
```

Variables opcionales segun uso:

```text
MAIL_HOST=REEMPLAZAR_SMTP_HOST
MAIL_PORT=587
MAIL_USER=REEMPLAZAR_MAIL_USER
MAIL_PASS=REEMPLAZAR_MAIL_PASSWORD
APP_ADMIN_EMAIL=REEMPLAZAR_EMAIL_ADMIN
AWS_S3_BUCKET=REEMPLAZAR_BUCKET_INFORMES
AWS_REGION=us-east-1
```

Flyway esta habilitado. Al arrancar en `prod`, el backend validara entidades y ejecutara migraciones pendientes sobre RDS.

## 4. Crear S3 + CloudFront Frontend

1. Crear bucket S3 privado para frontend: `REEMPLAZAR_BUCKET_FRONTEND`.
2. Subir solo contenido de `Producto/Frontend/dist`.
3. Crear CloudFront Distribution.
4. Usar Origin Access Control para que S3 no sea publico.
5. Configurar Default Root Object: `index.html`.
6. Configurar HTTPS con ACM si tienes dominio propio.
7. Para SPA React, configurar respuestas de error:
   - 403 -> `/index.html`, HTTP 200.
   - 404 -> `/index.html`, HTTP 200.

Esto evita 404 al refrescar rutas como `/dashboard` o `/morosidades`.

## 5. GitHub Actions con OIDC

Crear IAM OIDC provider para GitHub Actions y un IAM Role asumible por tu repositorio.

Trust policy orientativa:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::REEMPLAZAR_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:REEMPLAZAR_OWNER/REEMPLAZAR_REPO:*"
        }
      }
    }
  ]
}
```

Permisos minimos orientativos:

- Backend: `s3:PutObject`, `s3:GetObject`, `elasticbeanstalk:CreateApplicationVersion`, `elasticbeanstalk:UpdateEnvironment`, `elasticbeanstalk:Describe*`.
- Frontend: `s3:ListBucket`, `s3:PutObject`, `s3:DeleteObject`, `cloudfront:CreateInvalidation`.

## 6. Variables GitHub Actions

Configurar en GitHub: Repository Settings -> Secrets and variables -> Actions -> Variables.

| Variable | Donde se configura | Ejemplo | Obligatoria |
| --- | --- | --- | --- |
| `AWS_REGION` | GitHub Actions Variables | `us-east-1` | Si |
| `AWS_ROLE_TO_ASSUME` | GitHub Actions Variables | `arn:aws:iam::123456789012:role/github-actions-nma` | Si |
| `EB_APPLICATION_NAME` | GitHub Actions Variables | `NoMasAccidentes` | Backend |
| `EB_ENVIRONMENT_NAME` | GitHub Actions Variables | `NoMasAccidentes-prod` | Backend |
| `EB_S3_BUCKET_ARTIFACTS` | GitHub Actions Variables | `nma-eb-artifacts` | Backend |
| `S3_BUCKET_FRONTEND` | GitHub Actions Variables | `nma-frontend-prod` | Frontend |
| `CLOUDFRONT_DISTRIBUTION_ID` | GitHub Actions Variables | `E1234567890ABC` | Frontend |
| `VITE_API_URL` | GitHub Actions Variables | `https://URL_BACKEND_ELASTIC_BEANSTALK` | Frontend |

No uses `AWS_ACCESS_KEY_ID` ni `AWS_SECRET_ACCESS_KEY` salvo emergencia. El repositorio ya queda preparado para OIDC.

## 7. Variables Elastic Beanstalk

| Variable | Donde se configura | Ejemplo | Obligatoria |
| --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Elastic Beanstalk Environment properties | `prod` | Si |
| `DB_URL` | Elastic Beanstalk Environment properties | `jdbc:mysql://HOST_RDS:3306/no_mas_accidentes?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago` | Si |
| `DB_USER` | Elastic Beanstalk Environment properties o Secrets Manager integrado | `admin_nma` | Si |
| `DB_PASS` | Elastic Beanstalk Environment properties o Secrets Manager integrado | `REEMPLAZAR_PASSWORD` | Si |
| `JWT_SECRET` | Elastic Beanstalk Environment properties o Secrets Manager integrado | `REEMPLAZAR_SECRET_LARGO` | Si |
| `CORS_ALLOWED_ORIGINS` | Elastic Beanstalk Environment properties | `https://URL_CLOUDFRONT` | Si |
| `MAIL_HOST` | Elastic Beanstalk Environment properties | `smtp.gmail.com` | No |
| `MAIL_PORT` | Elastic Beanstalk Environment properties | `587` | No |
| `MAIL_USER` | Elastic Beanstalk Environment properties o Secrets Manager | `REEMPLAZAR` | No |
| `MAIL_PASS` | Elastic Beanstalk Environment properties o Secrets Manager | `REEMPLAZAR` | No |
| `APP_ADMIN_EMAIL` | Elastic Beanstalk Environment properties | `admin@dominio.cl` | No |
| `AWS_S3_BUCKET` | Elastic Beanstalk Environment properties | `nma-informes-prod` | No |

## 8. Deploy manual rapido backend

Desde la raiz del repo:

```bash
cd Producto/Backend
./mvnw clean package -DskipTests
```

En Windows PowerShell:

```powershell
cd Producto\Backend
.\mvnw.cmd clean package -DskipTests
```

Subir artifact y actualizar Elastic Beanstalk:

```bash
AWS_REGION=us-east-1
EB_APPLICATION_NAME=REEMPLAZAR_EB_APPLICATION_NAME
EB_ENVIRONMENT_NAME=REEMPLAZAR_EB_ENVIRONMENT_NAME
EB_S3_BUCKET_ARTIFACTS=REEMPLAZAR_BUCKET_ARTIFACTS
VERSION_LABEL=backend-$(date +%Y%m%d%H%M%S)
S3_KEY=elasticbeanstalk/backend/$VERSION_LABEL.jar

aws s3 cp target/NoMasAccidentes-0.0.1-SNAPSHOT.jar s3://$EB_S3_BUCKET_ARTIFACTS/$S3_KEY --region $AWS_REGION

aws elasticbeanstalk create-application-version \
  --application-name "$EB_APPLICATION_NAME" \
  --version-label "$VERSION_LABEL" \
  --source-bundle S3Bucket="$EB_S3_BUCKET_ARTIFACTS",S3Key="$S3_KEY" \
  --region $AWS_REGION

aws elasticbeanstalk update-environment \
  --environment-name "$EB_ENVIRONMENT_NAME" \
  --version-label "$VERSION_LABEL" \
  --region $AWS_REGION
```

## 9. Deploy manual rapido frontend

```bash
cd Producto/Frontend
npm ci
VITE_API_URL=https://URL_BACKEND_ELASTIC_BEANSTALK npm run build

aws s3 sync dist/ s3://REEMPLAZAR_BUCKET_FRONTEND --delete --region us-east-1
aws cloudfront create-invalidation --distribution-id REEMPLAZAR_CLOUDFRONT_DISTRIBUTION_ID --paths "/*"
```

En PowerShell:

```powershell
cd Producto\Frontend
npm ci
$env:VITE_API_URL="https://URL_BACKEND_ELASTIC_BEANSTALK"
npm run build
aws s3 sync dist/ s3://REEMPLAZAR_BUCKET_FRONTEND --delete --region us-east-1
aws cloudfront create-invalidation --distribution-id REEMPLAZAR_CLOUDFRONT_DISTRIBUTION_ID --paths "/*"
Remove-Item Env:\VITE_API_URL
```

## 10. Deploy con GitHub Actions

Backend:

1. Configurar variables GitHub de backend.
2. Ir a Actions -> `Deploy Backend to AWS Elastic Beanstalk`.
3. Ejecutar `Run workflow`.
4. Revisar logs de build, subida a S3 y update de Elastic Beanstalk.

Frontend:

1. Configurar variables GitHub de frontend.
2. Ir a Actions -> `Deploy Frontend to AWS S3 and CloudFront`.
3. Ejecutar `Run workflow`.
4. Revisar logs de build, sync S3 e invalidation CloudFront.

Para activar deploy automatico en `main`, descomenta el bloque `push` en cada workflow.

## 11. Checklist de validacion

- Backend levanta en Elastic Beanstalk.
- Backend conecta a RDS.
- Flyway ejecuta migraciones.
- `/swagger-ui.html` abre si esta permitido por red.
- Login responde.
- JWT funciona.
- Frontend carga desde CloudFront.
- Rutas internas como `/dashboard` y `/morosidades` refrescan sin 404.
- Frontend consume backend con `VITE_API_URL` correcto.
- CORS permite solo el dominio frontend esperado.
- HTTPS funciona.
- No hay secretos en GitHub ni en archivos del repo.
- `npm run build` funciona.
- `mvnw clean package -DskipTests` funciona.

## 12. Notas de seguridad

- No publiques RDS salvo que sea estrictamente necesario.
- Usa Secrets Manager o variables seguras de Elastic Beanstalk para secretos.
- Rota `JWT_SECRET` si alguna vez se expone.
- No uses `*` en CORS con JWT en produccion.
- Revisa costos de RDS, Elastic Beanstalk, S3, CloudFront y logs antes de dejar recursos encendidos.
