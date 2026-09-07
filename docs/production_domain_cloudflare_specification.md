# Simpatico Liquidations — Cloudflare Domain Specification & Verification Report

## 1. Executive Summary & Domain Topology

- **Primary Domain**: `https://simpaticoliquidations.com`
- **WWW Subdomain**: `https://www.simpaticoliquidations.com`
- **Production API Domain**: `https://api.simpaticoliquidations.com`
- **Authoritative Nameservers**: `conrad.ns.cloudflare.com`, `raina.ns.cloudflare.com`
- **Cloudflare Edge SSL**: Full (Strict) Mode with TLS 1.2+ minimum
- **Status**: **VERIFIED & OPERATIONAL**

---

## 2. Final DNS Audit & Routing Matrix

All DNS records for `simpaticoliquidations.com` are inventoried below. Unrelated email/MX and DMARC records were preserved without modification.

| Host / Subdomain | Type | Target / Value | Cloudflare Proxy | Purpose / Service |
| :--- | :---: | :--- | :---: | :--- |
| `@` (`simpaticoliquidations.com`) | `CNAME` / `A` | `remarkable-douhua-e8e8e9.netlify.app` | **Proxied** (Orange Cloud) | Netlify CDN Static Public Website |
| `www` | `CNAME` | `remarkable-douhua-e8e8e9.netlify.app` | **Proxied** (Orange Cloud) | Netlify CDN Static WWW Subdomain |
| `api` | `CNAME` | `simpatico-crm-api.onrender.com` | **Proxied** (Orange Cloud) | Production Spring Boot CRM API |
| `@` | `MX` | `route1.mx.cloudflare.net` (57) | DNS Only | Cloudflare Email Routing Inbound |
| `@` | `MX` | `route2.mx.cloudflare.net` (73) | DNS Only | Cloudflare Email Routing Inbound |
| `@` | `MX` | `route3.mx.cloudflare.net` (47) | DNS Only | Cloudflare Email Routing Inbound |
| `_dmarc` | `TXT` | `v=DMARC1; p=quarantine; rua=mailto:...` | DNS Only | Email Security DMARC Policy |

---

## 3. HTTPS & Edge Security Verification

1. **HTTP to HTTPS Redirect**: Tested `http://simpaticoliquidations.com` -> Returns `HTTP/1.1 301 Moved Permanently` pointing to `https://simpaticoliquidations.com/`.
2. **HTTPS Handshake**: Tested `https://simpaticoliquidations.com` -> Returns `HTTP/2 200 OK` with valid Cloudflare Universal SSL Certificate.
3. **Strict Transport Security (HSTS)**: `strict-transport-security: max-age=31536000; includeSubDomains; preload` header active.

---

## 4. Production CORS Hardening Verification

Spring Boot CORS controls (`application-prod.yml`) were verified against the production domain origin:

- **Allowed Origin 1 (`https://simpaticoliquidations.com`)**: `HTTP/1.1 200 OK`, `Access-Control-Allow-Origin: https://simpaticoliquidations.com`
- **Allowed Origin 2 (`https://www.simpaticoliquidations.com`)**: `HTTP/1.1 200 OK`, `Access-Control-Allow-Origin: https://www.simpaticoliquidations.com`
- **Disallowed Origin (`https://unauthorized-origin.org`)**: `HTTP/1.1 403 Forbidden` (`Invalid CORS request`)
- **Wildcard Policy**: Prohibited (Wildcard `*` disabled).

---

## 5. End-to-End Live Lead Pipeline Verification

Full end-to-end integration across production domain endpoints was verified:

```
[ https://simpaticoliquidations.com ] 
                   │
                   ▼ (POST /api/public/leads)
[ https://api.simpaticoliquidations.com ]
                   │
                   ▼ (Spring Boot JPA Hibernate Engine)
[ PostgreSQL DB: simpatico_crm_prod ]
                   │
                   ▼ (Admin CRM REST API & Dashboard)
[ CRM Admin Dashboard (GET /api/leads) ]
```

### Lead Verification Record
- **Lead Name**: Michael Scott
- **Company**: Dunder Mifflin Paper Wholesale
- **Email**: `michael.scott.domain@dundermifflin.com`
- **Category & Condition**: `APPAREL` / `NEW`
- **Budget / Quantity**: $120,000 / 500 Units
- **Lead ID Generated**: `2b08faf1-ac95-48c5-8dc2-c8f2ed0a7900`
- **Verification Status**: **PERSISTED & RETRIEVED FROM PRODUCTION CRM**
