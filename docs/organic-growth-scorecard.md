# Organic Growth Scorecard & Attribution Framework (Phase 23)

## 1. Organic Traffic & Lead Source Attribution Framework

To measure the real business impact of distribution channels without compromising user privacy, Simpatico Liquidations utilizes a clean, privacy-safe UTM attribution framework.

### Standardized UTM Structure
```text
https://simpaticoliquidations.com/resources/tools/pallet-profit-calculator.html?utm_source={channel}&utm_medium={format}&utm_campaign={topic}
```

### Approved Source/Medium Parameter Values

| Channel | `utm_source` | `utm_medium` | `utm_campaign` Example | Privacy Rule |
| :--- | :--- | :--- | :--- | :--- |
| **Reddit** | `reddit` | `community_answer` | `landed_cost_qna` | **NO PII IN UTMS** |
| **Facebook** | `facebook` | `group_post` | `truckload_vs_pallet` | **NO PII IN UTMS** |
| **LinkedIn** | `linkedin` | `organic_post` | `margin_breakdown` | **NO PII IN UTMS** |
| **YouTube** | `youtube` | `video_description` | `calculator_walkthrough` | **NO PII IN UTMS** |
| **Outreach** | `referral` | `partner_blog` | `reseller_tools_list` | **NO PII IN UTMS** |
| **Organic Search**| `google` | `organic` | `seo_buyer_guides` | **NO PII IN UTMS** |

---

## 2. Google Search Console Workflow & Feedback Loop

Search Console data is audited bi-weekly to systematically optimize high-opportunity pages:

```
PUBLISH ──► DISTRIBUTE ──► MEASURE ──► AUDIT SEARCH CONSOLE ──► OPTIMIZE CONTENT & CTAS
```

### Search Console Optimization Rules Matrix

| GSC Performance Pattern | Diagnosis | Required Strategic Action |
| :--- | :--- | :--- |
| **High Impressions / Low CTR** | Title tag or Meta Description isn't compelling in SERPs. | Rewrite `<title>` and `<meta name="description">` to emphasize practical value & hook search intent. |
| **High Impressions / Low Position (11-30)** | Page is recognized for topic but lacks depth or internal links. | Add detailed sub-sections, internal links from top pages, and structured data tables. |
| **Good Traffic / Low Conversion** | Page attracts readers but fails to guide them to landing page. | Improve contextual CTAs, introduce Pallet Profit Calculator widget embed, or refine landing page offer link. |
| **Good Traffic / High Conversion** | Page is a high-performing buyer acquisition asset. | Create supporting sub-topics, repurpose into social content, and prioritize for external outreach. |

---

## 3. Monthly Organic Scorecard Matrix

| Measurement Category | Specific Metric | Monthly Baseline | Target (30 Days) | Target (90 Days) | Primary Channel Attribution |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Traffic** | Organic Search Visitors | Baseline | +25% | +100% | Google Organic |
| | Referral Visitors | Baseline | +50% | +200% | Outreach & Directories |
| | Social Community Visitors | Baseline | +75% | +250% | Reddit, FB, LinkedIn, YouTube |
| | Top Resource Page Views | Baseline | +40% | +150% | All Distribution Channels |
| **Engagement** | Landing Page Visits (`#register`) | Baseline | +30% | +120% | Internal Resource Links |
| | Calculator Utilization Rate | Baseline | 35% | 50% | Pallet Profit Calculator |
| **Conversion** | Inbound Buyer Leads (API) | Baseline | +20% | +80% | Public Lead API |
| | Qualified Buyer Leads | Baseline | +15% | +60% | CRM Qualification SOP |
| | Resource-to-Lead Conversion Rate | Baseline | 2.5% | 4.0% | End-to-End Funnel |
| **Distribution** | Community Contributions | 0 | 12 / mo | 25 / mo | Reddit & FB Manual Posts |
| | Active Outreach Contacts | 0 | 15 / mo | 45 / mo | `backlink-strategy.md` |
| | Earned Backlinks | Baseline | 3 / mo | 10 / mo | Verified External Links |
| **Business** | Qualified Wholesale Opportunities | Baseline | +15% | +50% | Sales CRM Pipeline |

---

## 4. Phase 23 Final Organic Acquisition Scorecard

| Area | Status | Verification Notes |
| :--- | :--- | :--- |
| **Phase 22 SEO Foundation** | **PASS** | Index, buyer guides, calculator, sitemap, robots.txt verified functional. |
| **Resource Distribution** | **PASS** | Distribution inventory matrix created; top resources prioritized. |
| **Reddit Strategy** | **READY** | Human-only, non-spam 80/20 value Q&A playbook established. |
| **Facebook Strategy** | **READY** | Reseller/bin-store community participation protocol established. |
| **LinkedIn Strategy** | **READY** | Educational & data-driven post formats and templates created. |
| **YouTube Strategy** | **READY** | Video script outlines & resource description funnel documented. |
| **Outreach Strategy** | **READY** | Value-first publisher outreach template and prospect pipeline ready. |
| **Backlink Strategy** | **READY** | Earned backlink strategy & directory evaluation framework active. |
| **Linkable Assets** | **PASS** | Pallet Profit Calculator & Manifest guide ranked as top assets. |
| **Internal Tracking** | **PASS** | Privacy-safe UTM attribution structure configured. |
| **Analytics** | **PASS** | Event tracking verified (zero PII transmitted). |
| **Source Attribution** | **PASS** | Inbound lead source preserved in Spring Boot API & CRM model. |
| **CRM Integration** | **PASS** | Direct flow from landing page to `simpatico_crm_prod` database verified. |
| **Conversion Tracking** | **PASS** | Triggered strictly upon HTTP 200/201 API response. |
| **Privacy** | **PASS** | PII strictly isolated; no outreach data in buyer tables. |
| **SEO Safety** | **PASS** | No doorway pages, no link farms, no duplicate content created. |
| **Documentation** | **PASS** | All Phase 23 documentation files created in `docs/`. |
| **30-Day Plan** | **PASS** | 30-day non-automated operational calendar established. |

---

## 5. Future Phase Opportunities Roadmap (Phases 24 - 28)

* **PHASE 24 — SEO Tools, Calculators & Linkable Assets**:
  * Develop full Landed-Cost & Margin Calculator suite.
  * Interactive Manifest Audit Tool.
  * Downloadable Wholesale Buyer Checklists (PDF).
* **PHASE 25 — Organic Conversion Optimization**:
  * Resource page CTA A/B positioning tests.
  * Landing page conversion rate optimization (CRO).
* **PHASE 26 — Lead Qualification & Buyer Intelligence**:
  * Automated lead scoring based on budget and purchase frequency.
  * Aggregated buyer demand analytics dashboard.
* **PHASE 27 — Authority & Backlink Development**:
  * Annual Wholesale Liquidation Industry Data Report.
  * Trade publication guest column series.
* **PHASE 28 — Controlled Paid Acquisition**:
  * Highly targeted Search Ad campaigns (only when financially feasible).
