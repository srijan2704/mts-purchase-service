# MTS Purchase Service

Spring Boot backend service for the MTS Finance Dashboard.  
It manages authentication, sellers, products/variants, purchase orders, and reporting APIs.

## 1. Tech Stack

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA + Hibernate
- Oracle JDBC (ojdbc11)
- HikariCP connection pool
- Springdoc OpenAPI (Swagger UI)
- Maven Wrapper (`./mvnw`)

## 2. Key Features

- Token-based authentication (`Bearer <token>`)
- OTP-based user registration flow (admin email approval)
- HTTPS enforcement on auth endpoints (configurable)
- Auth rate limiting (login + OTP) per client IP
- CRUD for:
  - sellers
  - product types
  - units
  - products and variants
  - purchase orders
- Reporting APIs (daily summary, trends, seller history)
- Actuator health endpoint for deployment checks

## 3. API Surface (High Level)

Base path: `/api`

- Auth: `/api/auth/*`
- Sellers: `/api/sellers/*`
- Product types: `/api/product-types/*`
- Units: `/api/units/*`
- Products: `/api/products/*`
- Variants: `/api/variants/*` and `/api/products/{id}/variants`
- Purchase orders: `/api/purchase-orders/*`
- Reports: `/api/reports/*`

OpenAPI/Swagger:
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

Health:
- `http://localhost:8080/actuator/health`

## 4. Prerequisites

- JDK 21
- Maven (optional; wrapper is included)
- Oracle DB (local/dev or remote UAT/PROD)

## 5. Environment Variables

Do not commit real secrets. Use local `.env` (already gitignored) or OS environment variables.

Minimum required variables:

```bash
# Runtime profile: dev | uat | prod
PROJ_ENVIRONMENT=dev

# Database
DB_USERNAME=<your_db_user>
DB_PASSWORD=<your_db_password>

# Mail/OTP
MAIL_USERNAME=<your_smtp_username>
MAIL_PASSWORD=<your_smtp_password_or_app_password>
AUTH_REG_OTP_FROM_EMAIL=<sender_email>
AUTH_REG_OTP_OWNER_EMAIL=<owner_approval_email>

# Security
AUTH_TRANSPORT_REQUIRE_HTTPS=false
AUTH_RATE_LIMIT_ENABLED=true
AUTH_RATE_LIMIT_MAX_REQUESTS=5
AUTH_RATE_LIMIT_WINDOW_SECONDS=30

# CORS allowlist for browser clients
APP_CORS_ALLOWED_ORIGINS=http://localhost:5500,http://127.0.0.1:5500
```

Profile-specific DB URL is read from:
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-uat.properties`
- `src/main/resources/application-prod.properties`

## 6. Run Locally

1. Create `.env` in project root with required variables.
2. Start app:

```bash
./mvnw spring-boot:run
```

Build jar:

```bash
./mvnw clean package -DskipTests
```

Run tests:

```bash
./mvnw test
```

Run packaged jar:

```bash
java -jar target/mts-purchase-service-0.0.1-SNAPSHOT.jar
```

## 7. Auth Notes

- Public auth endpoints:
  - `POST /api/auth/setup`
  - `POST /api/auth/register/request-otp`
  - `POST /api/auth/register/verify-otp`
  - `POST /api/auth/login`
- Protected API routes use `Authorization: Bearer <token>`.
- `POST /api/auth/logout` revokes active session token.
- `GET /api/auth/me` validates and refreshes session expiry.

## 8. Logging and Observability

- Log file: `logs/mts.log`
- Request logging filter logs inbound/outbound API calls.
- Useful checks:

```bash
curl -I http://127.0.0.1:8080/actuator/health
curl -I http://127.0.0.1:8080/swagger-ui/index.html
```

## 9. Database Scripts

Initial schema SQL scripts are under:

- `src/main/resources/db-scripts/`

Apply in order of file prefix (`01_...`, `02_...`, etc.) for fresh DB setup.

## 10. Docker

Build image:

```bash
docker build -t mts-purchase-service .
```

Run container:

```bash
docker run --rm -p 8080:8080 \
  -e PROJ_ENVIRONMENT=dev \
  -e DB_USERNAME=<your_db_user> \
  -e DB_PASSWORD=<your_db_password> \
  mts-purchase-service
```

## 11. EC2 Deployment (Current Repo Setup)

Manual GitHub Actions workflow:
- `.github/workflows/deploy-ec2.yml`

Remote deploy helper script:
- `scripts/ec2-deploy.sh`

Workflow uses repository secrets:
- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_PRIVATE_KEY`
- `EC2_KNOWN_HOSTS`


## 12. Security Guidelines

- Never commit `.env`, wallet files, private keys, or credentials.
- Keep `AUTH_TRANSPORT_REQUIRE_HTTPS=true` for UAT/PROD.
- Keep auth rate limiting enabled in non-local environments.
- Restrict CORS allowlist to trusted UI origins only.
- Rotate DB/SMTP credentials immediately if exposed.

