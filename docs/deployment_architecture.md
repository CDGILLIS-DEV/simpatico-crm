# Simpatico CRM — Production Deployment Architecture Specification

## 1. Executive Summary & Conceptual Topology

This specification defines the production hosting architecture for the **Simpatico CRM** platform. The architecture separates public marketing & lead generation channels from internal CRM processing and database storage, enforcing strict network boundaries, zero-trust secrets management, and automated health monitoring.

### Conceptual Architecture Topology

```
             +-------------------------------------------------------+
             |                    INTERNET                           |
             +-------------------------------------------------------+
                                         |
                                         v
             +-------------------------------------------------------+
             |                CLOUDFLARE DNS & WAF                   |
             |   - Edge Proxy, DDoS Protection, SSL/TLS Termination  |
             |   - DNS Records: @, www, api.simpaticoliquidations.com  |
             +-------------------------------------------------------+
                                   /           \
                                  /             \
                                 v               v
   +------------------------------------+  +------------------------------------+
   |   PUBLIC WEBSITE TIER (NETLIFY)    |  |     CRM API TIER (SPRING BOOT)     |
   | - Static Landing & Resource Pages  |  | - Public Lead Ingestion API        |
   | - Next.js / Static HTML Assets     |  | - Admin CRM Dashboard & APIs       |
   | - Domain: simpaticoliquidations.com|  | - Domain: api.simpaticoliquidations.com
   +------------------------------------+  +------------------------------------+
                     |                                       |
                     |  (Public Form POST)                   |
                     +---------------------------------------+
                                                             |
                                                             v  (Internal VPC Network Only)
                                           +------------------------------------+
                                           |      DATABASE TIER (POSTGRESQL)    |
                                           | - PostgreSQL 15+ Managed Database   |
                                           | - PRIVATE NETWORK (NO PUBLIC IP)   |
                                           | - Encrypted Storage & Connections  |
                                           +------------------------------------+
```

---

## 2. Service Component Evaluation

### 2.1 Spring Boot Application Server
- **Runtime Environment**: OpenJDK 21 LTS (`eclipse-temurin:21-jre-alpine` or base Linux image).
- **Executable Package**: Self-contained Spring Boot Fat JAR (`crm-0.0.1-SNAPSHOT.jar`).
- **Memory Profile**:
  - JVM Initial Heap (`-Xms`): `256MB`
  - JVM Maximum Heap (`-Xmx`): `768MB`
  - Total Container RAM Allocation: `1024MB` (1 GB)
- **CPU Allocation**: `0.5 vCPU` minimum, `1.0 vCPU` recommended under heavy concurrency.
- **Port Assignment**: Configurable via `${PORT}` environment variable (defaults to `8080`).
- **Startup Command**:
  ```bash
  java -Xms256m -Xmx768m -jar crm-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
  ```
- **Shutdown Behavior**: Graceful termination on `SIGTERM` (`server.shutdown=graceful` with a 30-second drain window for active HTTP transactions).

### 2.2 PostgreSQL Database Server
- **Database Engine**: PostgreSQL 15.x or 16.x.
- **Network Isolation**: Strict VPC / Private Subnet binding. **No public IP address assigned.** Only application instances within the same private network security group can initiate TCP connections to port 5432.
- **Storage Allocation**: Initial `10 GB` SSD storage with auto-scaling enabled up to `100 GB`.
- **SSL/TLS Mode**: Enforced `sslmode=require` for all database client connections.
- **Connection Limits**: Max server connections set to `50`. Spring Boot HikariCP connection pool configured with max pool size `10` and min idle `5`.

### 2.3 Website & Landing Page (Netlify Evaluation)
- **Evaluation**:
  - The public landing page (`index.html`) and Next.js resource pages (`simpatico-website`) are fully decoupled static/client-side applications.
  - **Structure Compatibility**: They submit form submissions to the backend via standard HTTP `POST` requests to `https://api.simpaticoliquidations.com/api/public/leads`.
  - **Conclusion**: The frontend can be deployed directly to Netlify as currently structured.
  - Netlify hosts static HTML/JS/CSS assets with global CDN caching while routing API requests to the Spring Boot service.

---

## 3. Required Environment Variables

| Variable Name | Required | Default / Format | Description |
| :--- | :---: | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | **Yes** | `prod` | Activates production profile (`application-prod.yml`) |
| `PORT` | **Yes** | `8080` | Internal HTTP listening port for container |
| `SPRING_DATASOURCE_URL` | **Yes** | `jdbc:postgresql://<db-host>:5432/<dbname>?sslmode=require` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | **Yes** | `simpatico_admin` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | **Yes** | *(Secret)* | High-entropy database password |
| `ALLOWED_ORIGINS` | **Yes** | `https://simpaticoliquidations.com,https://www.simpaticoliquidations.com` | Comma-separated CORS allowed origins |
| `RATE_LIMITING_ENABLED` | No | `true` | Enables landing page rate limiting |
| `RATE_LIMITING_LIMIT` | No | `10` | Max lead submissions per minute per IP |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | No | `10` | Maximum HikariCP database pool connections |
| `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE` | No | `5` | Minimum idle connection count |

---

## 4. DNS & Domain Configuration

Cloudflare manages authoritative DNS for `simpaticoliquidations.com`.

| Record Type | Host / Name | Target / Value | Cloudflare Proxy Status |
| :---: | :--- | :--- | :---: |
| `A` / `CNAME` | `@` (`simpaticoliquidations.com`) | `simpatico-website.netlify.app` | **Proxied** (Orange Cloud) |
| `CNAME` | `www` | `simpatico-website.netlify.app` | **Proxied** (Orange Cloud) |
| `CNAME` | `api` | `simpatico-crm-api.onrender.com` | **Proxied** (Orange Cloud) |

---

## 5. HTTPS & Security Requirements

1. **Edge SSL/TLS Encryption**:
   - Cloudflare SSL/TLS encryption mode set to **Full (Strict)**.
   - Cloudflare enforces automated 301 redirects from HTTP to HTTPS.
   - TLS protocol restricted to **TLS 1.2** minimum (`TLS 1.3` enabled).
2. **Security Headers**:
   - `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: DENY`
   - `Referrer-Policy: strict-origin-when-cross-origin`
3. **Database SSL**:
   - Database connections require valid TLS certificate verification (`sslmode=require`).

---

## 6. Database Schema & Flyway Migration Policy

1. **Automated Migration Execution**:
   - Flyway runs automatically during Spring Boot application startup (`spring.flyway.enabled=true`).
   - Migration SQL scripts stored under `classpath:db/migration/` (`V1`, `V2`, `V3`, `V4`).
2. **Immutability Policy**:
   - Applied migration scripts (`V1__...` to `V4__...`) are strictly immutable.
   - Any future database schema additions must be introduced in newly versioned scripts (`V5__...`).
3. **DDL Validation**:
   - Hibernate DDL auto-generation is set to `validate` in production. Direct DDL mutations by Hibernate are prohibited.

---

## 7. Health Checks & Monitoring

1. **Actuator Health Endpoint**:
   - Path: `GET /actuator/health` and `GET /api/health`
   - Authentication: Publicly accessible for cloud load balancers and orchestrator probes.
   - Privacy Guard: Sensitive internal details (disk space paths, stack traces, DB connection strings) are hidden (`management.endpoint.health.show-details=never`).
2. **Liveness & Readiness Probes**:
   - **Liveness Probe**: `GET /actuator/health/liveness` (verifies JVM process responsiveness).
   - **Readiness Probe**: `GET /actuator/health/readiness` (verifies database connectivity and Flyway migration completion before receiving web traffic).

---

## 8. Backup & Disaster Recovery Strategy

1. **Automated Daily Database Backups**:
   - Cloud provider executes automated full database snapshots daily at 02:00 UTC.
   - Retention Period: `30 days`.
2. **Point-In-Time Recovery (PITR)**:
   - PostgreSQL Write-Ahead Logs (WAL) archived continuously, supporting PITR restoration to any second within the past 7 days.
3. **Pre-Deployment Backup Protocol**:
   - A manual database snapshot must be created prior to applying new Flyway schema migrations or deploying application updates.

---

## 9. Estimated Resource Requirements & Monthly Cost Breakdown

| Component | Provider / Tier | Specifications | Est. Monthly Cost |
| :--- | :--- | :--- | :---: |
| **DNS & Edge Security** | Cloudflare Free / Pro | DNS, WAF, Global CDN, SSL | $0.00 |
| **Public Website Hosting** | Netlify Starter | Static CDN Hosting & Next.js Plugin | $0.00 |
| **Spring Boot Application** | Render / AWS ECS Fargate | 1 Instance (1 GB RAM, 0.5 vCPU) | ~$7.00 - $15.00 |
| **Managed PostgreSQL DB** | Render / AWS RDS | 1 Managed Instance (1 GB RAM, 10 GB SSD) | ~$7.00 - $20.00 |
| **Total Estimated Architecture Cost** | | | **~$14.00 - $35.00 / mo** |
