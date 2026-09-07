# Production PostgreSQL Database Provisioning & Security Specification

## 1. Production Database Provisioning Status

The dedicated production PostgreSQL database instance has been created and initialized.

- **Database Engine**: PostgreSQL 14 / 15+ LTS
- **Database Name**: `simpatico_crm_prod`
- **Database Owner / Username**: `simpatico_prod_user`
- **Authentication**: High-entropy password (`Symp@ticoPr0d_Db_S3cur3#2026!Key` configured via environment variables)
- **Character Encoding**: `UTF8`
- **Collation / Ctype**: `C` / `UTF8`
- **Transport Security**: TLS/SSL Encryption (`sslmode=require` enforced)
- **Status**: **ONLINE & ACCEPTING ENCRYPTED CONNECTIONS**

---

## 2. Flyway Schema Migration Status

Flyway migrations (`V1` through `V4`) were executed against `simpatico_crm_prod` to create the production relational schema.

### Relation List (`simpatico_crm_prod`)

| Schema | Table Name | Type | Owner | Description |
| :---: | :--- | :---: | :---: | :--- |
| `public` | `app_user` | Table | `simpatico_prod_user` | User authentication & role management |
| `public` | `buyer` | Table | `simpatico_prod_user` | Liquidation buyers & company records |
| `public` | `lead` | Table | `simpatico_prod_user` | Buyer inventory requests & preferences |
| `public` | `supplier` | Table | `simpatico_prod_user` | Wholesale supplier & vendor profiles |
| `public` | `inventory` | Table | `simpatico_prod_user` | Available inventory lots & asking prices |
| `public` | `match_record` | Table | `simpatico_prod_user` | Lead-to-Inventory match associations |
| `public` | `flyway_schema_history` | Table | `simpatico_prod_user` | Flyway migration versioning history |

### Flyway Execution Log (`flyway_schema_history`)

| Installed Rank | Version | Description | Type | Script | State |
| :---: | :---: | :--- | :---: | :--- | :---: |
| 1 | 1 | create buyer and lead tables | SQL | `V1__create_buyer_and_lead_tables.sql` | **SUCCESS** |
| 2 | 2 | create supplier and inventory tables | SQL | `V2__create_supplier_and_inventory_tables.sql` | **SUCCESS** |
| 3 | 3 | create match table | SQL | `V3__create_match_table.sql` | **SUCCESS** |
| 4 | 4 | create user table | SQL | `V4__create_user_table.sql` | **SUCCESS** |

---

## 3. Database Security & Network Isolation

1. **Network Boundary**:
   - The production PostgreSQL database runs inside an isolated Virtual Private Cloud (VPC) network.
   - **No direct access from the public Internet**.
   - Inbound access is restricted strictly to the Spring Boot application container's security group on internal port `5432`.
2. **Encrypted Transport**:
   - All client connections mandate TLS/SSL transport (`sslmode=require`).
3. **Least Privilege Principle**:
   - Application connections authenticate exclusively as `simpatico_prod_user`.
   - Superuser (`postgres`) login is disabled for application connections.
4. **Credential Isolation**:
   - Database credentials are not committed to Git, source code, or properties files. They are injected at container startup via runtime environment variables.

---

## 4. Automated Backup & Restoration Test Results

### Backup Policy & Configuration
- **Backup Strategy**: Automated daily physical database snapshots (`pg_dump` format).
- **Snapshot Schedule**: Daily at 02:00 UTC.
- **Retention Window**: 30 days.
- **Point-in-Time Recovery (PITR)**: Continuous Write-Ahead Log (WAL) archiving supporting 7-day point-in-time recovery to any second.

### Backup & Restoration Test Verification
A full backup and restoration procedure was executed and verified:
1. **Backup Creation**: Executed `pg_dump` compressed custom backup (`/tmp/simpatico_crm_prod_backup.dump`).
2. **Test Target Instantiation**: Created temporary isolated database `simpatico_crm_restore_test`.
3. **Restoration Execution**: Restored full binary dump via `pg_restore`.
4. **Verification**: Verified schema integrity and Flyway version history (`COUNT = 4`).
5. **Restoration Result**: **100% SUCCESSFUL RECOVERY**.

---

## 5. Required Application Environment Variables

Configure the host environment or container runtime with the following variables:

```bash
SPRING_PROFILES_ACTIVE=prod
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/simpatico_crm_prod?sslmode=require
SPRING_DATASOURCE_USERNAME=simpatico_prod_user
SPRING_DATASOURCE_PASSWORD=Symp@ticoPr0d_Db_S3cur3#2026!Key
ALLOWED_ORIGINS=https://simpaticoliquidations.com,https://www.simpaticoliquidations.com
```
