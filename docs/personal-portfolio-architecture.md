# Personal Portfolio Investment Tracker
## Architecture and Implementation Blueprint

Version: 1.0  
Date: 2026-03-07  
Target stack: JavaScript Frontend + Java Spring Boot Backend + Oracle Database

## 1. Goal
Build a personal wealth dashboard that tracks:
- Debt Mutual Funds
- Equity Mutual Funds
- EPF
- Indian Stocks (HDFC Securities)
- US Stocks (INDmoney)
- Bitcoin (CoinDCX)
- Fixed Deposits (multiple banks)

Core requirement: auto-update data wherever APIs are available; use import/manual fallbacks where APIs are unavailable.

## 2. Product Scope
### 2.1 Key outcomes
- Single net-worth dashboard across all asset classes.
- Daily valuation updates with historical trend.
- Portfolio allocation by asset class, account, and geography (India vs US).
- Transaction history and realized/unrealized gain tracking.
- Reconciliation workflow for accounts without stable APIs.

### 2.2 Out of scope (phase 1)
- Live order placement/trading.
- Tax filing automation.
- Multi-user tenancy.

## 3. Data Source Strategy
Use a connector policy with three source tiers:

| Asset | Primary ingestion mode | Auto-update level | Notes |
|---|---|---|---|
| Debt MF | Public NAV API/file feed | High | Update daily NAV + valuation |
| Equity MF | Public NAV API/file feed | High | Same as debt MF |
| EPF | Statement import/manual sync | Low-Medium | No stable public personal API expected |
| Indian Stocks (HDFC) | Holdings import + public market quote API | Medium | Price auto, holdings usually import/manual |
| US Stocks (INDmoney) | Holdings import + US quote API | Medium | Price auto, holdings import/manual |
| Bitcoin (CoinDCX) | Exchange public ticker API + trade export | High | Price near real-time, holdings via API/export |
| Fixed Deposits | Manual entry + maturity calculator | Low | Principal/rate/tenure tracked internally |

Implementation rule:
- Prefer official/public APIs first.
- If API is absent/unstable, support CSV/PDF import flow and manual correction.
- Persist raw payloads for audit and debugging.

## 4. High-Level Architecture
```text
[JS Frontend SPA]
   |
   v
[Spring Boot API]
   |-- Auth + User Preferences
   |-- Portfolio Domain APIs
   |-- Connector Orchestrator
   |-- Valuation Engine
   |-- Reconciliation Service
   |
   +--> [Oracle DB]
   +--> [Scheduler/Job Runner]
   +--> [External APIs / Import Adapters]
```

### 4.1 Backend logical modules
- `auth`: local auth/session/JWT.
- `portfolio-core`: accounts, instruments, holdings, transactions.
- `connectors`: per source adapters (MF, CoinDCX, quote APIs, importers).
- `valuation`: NAV/price ingestion, FX conversion, position valuation.
- `analytics`: time-series snapshots, XIRR/CAGR calculators.
- `recon`: mismatch detection and correction suggestions.
- `notification` (optional phase 2): threshold alerts.

## 5. Oracle Data Model (Core Tables)
### 5.1 Master
- `USER_PROFILE` (`USER_ID`, `NAME`, `BASE_CURRENCY`, `TIMEZONE`)
- `ACCOUNT` (`ACCOUNT_ID`, `USER_ID`, `ACCOUNT_TYPE`, `BROKER_NAME`, `MASKED_NUMBER`, `SOURCE_MODE`)
- `INSTRUMENT` (`INSTRUMENT_ID`, `ASSET_CLASS`, `SYMBOL`, `ISIN`, `NAME`, `CURRENCY`, `COUNTRY`)

### 5.2 Transactions and holdings
- `TXN_LEDGER` (`TXN_ID`, `ACCOUNT_ID`, `INSTRUMENT_ID`, `TXN_TYPE`, `TXN_DATE`, `QUANTITY`, `UNIT_PRICE`, `TOTAL_AMOUNT`, `FEES`, `RAW_REF`)
- `HOLDING_POSITION` (`POSITION_ID`, `ACCOUNT_ID`, `INSTRUMENT_ID`, `AS_OF_DATE`, `QUANTITY`, `AVG_COST`, `COST_VALUE`)
- `VALUATION_SNAPSHOT` (`SNAPSHOT_ID`, `AS_OF_TS`, `ACCOUNT_ID`, `INSTRUMENT_ID`, `MARKET_PRICE`, `FX_RATE`, `MARKET_VALUE`, `UNREALIZED_PNL`)

### 5.3 Market and sync
- `PRICE_POINT` (`PRICE_ID`, `INSTRUMENT_ID`, `PRICE_DATE`, `PRICE`, `SOURCE`, `RAW_PAYLOAD_ID`)
- `FX_RATE` (`FX_ID`, `BASE_CCY`, `QUOTE_CCY`, `RATE_DATE`, `RATE`, `SOURCE`)
- `SYNC_JOB` (`JOB_ID`, `JOB_TYPE`, `SOURCE_NAME`, `STARTED_AT`, `ENDED_AT`, `STATUS`, `SUMMARY`)
- `RAW_PAYLOAD` (`RAW_PAYLOAD_ID`, `SOURCE_NAME`, `FETCHED_AT`, `HASH`, `PAYLOAD_CLOB`)
- `RECON_ISSUE` (`ISSUE_ID`, `SEVERITY`, `ACCOUNT_ID`, `INSTRUMENT_ID`, `DETECTED_AT`, `STATUS`, `DETAILS`)

### 5.4 Constraints/indexing
- Unique key: (`ACCOUNT_ID`, `INSTRUMENT_ID`, `AS_OF_DATE`) on `HOLDING_POSITION`.
- Unique key: (`INSTRUMENT_ID`, `PRICE_DATE`, `SOURCE`) on `PRICE_POINT`.
- Partition `VALUATION_SNAPSHOT` monthly for scalable trend queries.
- Index `TXN_LEDGER` on (`ACCOUNT_ID`, `TXN_DATE`) and (`INSTRUMENT_ID`, `TXN_DATE`).

## 6. Update and Sync Architecture
### 6.1 Job schedule (example)
- `06:00 IST`: Mutual fund NAV refresh.
- `Every 15 min (market hours)`: Indian/US quote refresh.
- `Every 5 min`: Crypto quote refresh.
- `22:30 IST`: End-of-day valuation snapshot.
- `On-demand`: manual import parse + reconcile.

### 6.2 Job execution pattern
1. Insert `SYNC_JOB` row as `RUNNING`.
2. Fetch data with connector adapter.
3. Store payload in `RAW_PAYLOAD`.
4. Transform + validate + upsert `PRICE_POINT` / `TXN_LEDGER`.
5. Recompute impacted holdings.
6. Write `VALUATION_SNAPSHOT`.
7. Mark job `SUCCESS` or `FAILED` with summary/error.

### 6.3 Reliability controls
- Retry with exponential backoff for transient API failures.
- Circuit-breaker per connector.
- Idempotency key for each ingest batch (`source + timestamp + hash`).
- Dead-letter queue table (`SYNC_JOB` failures + stack traces).

## 7. Backend API Design (v1)
### 7.1 Dashboard
- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/networth-trend?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/v1/dashboard/allocation`

### 7.2 Holdings and transactions
- `GET /api/v1/holdings?assetClass=...&accountId=...`
- `GET /api/v1/holdings/{instrumentId}/history`
- `GET /api/v1/transactions?accountId=...&from=...&to=...`
- `POST /api/v1/transactions/manual`

### 7.3 Connectors and sync
- `GET /api/v1/connectors`
- `POST /api/v1/connectors/{connectorKey}/sync`
- `GET /api/v1/sync-jobs?status=...`
- `POST /api/v1/imports/{sourceType}` (CSV upload)

### 7.4 Reconciliation
- `GET /api/v1/reconciliation/issues`
- `POST /api/v1/reconciliation/issues/{issueId}/resolve`

## 8. Frontend Architecture (JavaScript)
Recommended structure (vanilla JS or modular JS):
```text
/assets/js
  /api
    client.js
    portfolio-api.js
    connector-api.js
  /state
    store.js
  /pages
    dashboard.js
    holdings.js
    transactions.js
    integrations.js
    reconciliation.js
  /components
    cards.js
    table.js
    chart.js
    sync-status.js
```

Frontend responsibilities:
- Route to feature pages.
- Render data grids and charts.
- Show connector sync state, last updated timestamps, and stale-data banners.
- Trigger manual import and sync actions.

## 9. UI Pages to Build
1. Dashboard
- Net worth card, day change, month change.
- Allocation donut (asset class + account).
- Latest sync status panel by connector.
- Trend chart (30D/90D/1Y).

2. Holdings
- Filter by asset class/account/country.
- Table: quantity, avg cost, price, current value, PnL.
- Drill-down to instrument transaction history.

3. Transactions
- Unified ledger with tags (`BUY`, `SELL`, `DIVIDEND`, `INTEREST`, `FD_MATURITY`).
- Upload/import buttons by source (HDFC, INDmoney, EPF statement, CoinDCX export).

4. Integrations
- Connector cards with:
  - mode (`API`, `IMPORT`, `MANUAL`)
  - last success
  - next schedule
  - manual sync button

5. Reconciliation
- Data mismatches, missing prices, and duplicate transactions.
- Resolve flow (accept suggestion, edit manually, ignore with reason).

## 10. Security and Compliance
- Spring Security with JWT/session token.
- Encrypt sensitive credentials using JCE + externalized secrets.
- Never store broker passwords in plaintext.
- Audit log for imports, manual edits, and reconciliation.
- PII minimization (mask account numbers in UI).

## 11. Deployment Blueprint
### 11.1 Runtime
- Frontend: static JS app via Nginx.
- Backend: Spring Boot service (`systemd` or container).
- Oracle DB: existing environment.

### 11.2 Environment configuration
- Connector API keys in env vars/secret manager.
- Profile-based config (`dev`, `uat`, `prod`).
- Scheduler toggles by profile.

### 11.3 Observability
- Structured logs (`sync_job_id`, `connector`, `duration_ms`).
- Metrics: sync success rate, stale connector count, valuation latency.
- Health checks: `/actuator/health`, connector status endpoint.

## 12. Phased Delivery Plan
### Phase 1 (MVP: 4-6 weeks)
- Account/instrument/transaction model
- Manual imports (HDFC, INDmoney, EPF, CoinDCX CSV)
- Mutual fund NAV + equity/crypto quote auto-price
- Dashboard + holdings + transactions pages

### Phase 2
- Connector automation for available APIs
- Reconciliation engine
- Alerting for stale data and allocation drift

### Phase 3
- Advanced analytics (XIRR, goal tracking, forecast)
- Mobile-first enhancements

## 13. Key Risks and Mitigations
- API availability/legal limits: keep import fallback and adapter isolation.
- Data inconsistency across brokers: reconciliation workflow + audit trail.
- FX conversion drift for US assets: store daily FX rates and valuation timestamp.
- Incomplete historical data: support backfill imports.

## 14. Definition of Done (MVP)
- Daily net worth updates are visible on dashboard.
- At least 80% valuation auto-refresh (prices) for supported assets.
- Unsupported API assets are still trackable via import/manual flow.
- Rebuildable and deployable with your existing JS + Spring Boot + Oracle stack.
