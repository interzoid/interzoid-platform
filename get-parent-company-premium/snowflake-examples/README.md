# Parent Company Information with Snowflake

This directory contains **Snowflake integration examples** for the **Interzoid Parent Company Information (Premium) API**:
https://www.interzoid.com/apis/get-parent-company-info

These examples show how to enrich company records inside Snowflake with **authoritative parent company intelligence**, enabling improved **data enrichment, entity resolution, hierarchy analysis, and enterprise reporting**.

---

## Overview

Organizations often operate under complex corporate hierarchies that include parent companies, subsidiaries, and holding entities. In many datasets, only the subsidiary or operating company name is present, making it difficult to understand true corporate ownership.

The Interzoid Parent Company Information API returns **parent company and corporate hierarchy data** for a given company name. By enriching records with this information in Snowflake, organizations can unify data across subsidiaries, improve rollups, and gain clearer insight into corporate structures.

Product documentation (authoritative reference for this directory):
https://www.interzoid.com/apis/get-parent-company-info

---

## What This Directory Demonstrates

The Snowflake examples in this directory show how to:

- retrieve parent company information using the Interzoid API
- enrich Snowflake tables with parent company attributes
- normalize corporate hierarchies across datasets
- improve entity resolution and company rollups
- automate enrichment workflows in Snowflake pipelines

These patterns support both **one-time enrichment** and **automated ELT / ETL workflows**.

---

## Common Parent Company Enrichment Use Cases

### Data Enrichment
- add parent company details to CRM, ERP, and vendor datasets
- enhance company profiles with ownership intelligence
- improve completeness of enterprise reference data

### Entity Resolution and Hierarchy Mapping
- group subsidiaries under a common parent company
- understand corporate structures for analytics and governance
- reduce fragmentation caused by multiple operating entities

### Analytics and Reporting
- roll up revenue, spend, or risk by parent company
- improve account segmentation and reporting accuracy
- enable parent-level KPIs and dashboards

### Compliance, Risk, and Procurement
- identify ultimate parent entities for compliance workflows
- support vendor risk and third-party due diligence
- analyze exposure across related corporate entities

---

## How It Works in Snowflake

1. Load company or organization names into a Snowflake table.
2. Call the Interzoid Parent Company Information API.
3. Store returned parent company attributes in Snowflake columns.
4. Use SQL to group, filter, and report by parent company.

### Example SQL Pattern

```sql
SELECT
  PARENT_COMPANY_NAME,
  COUNT(*) AS SUBSIDIARY_COUNT
FROM COMPANY_TABLE
GROUP BY PARENT_COMPANY_NAME
ORDER BY SUBSIDIARY_COUNT DESC;
