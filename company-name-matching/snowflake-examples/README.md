# Company Name Matching with Snowflake

Struggling with organization name differences such as **IBM** & **International Business Machines**? **GE Corp** & **General Electric**? **L.A. County Schools** and **Los Angeles Schools**? Misspellings? Variations? International data?

This directory contains **Snowflake integration examples** for the **Interzoid Company Name Matching API**, designed to help data engineers, analytics teams, and platform architects identify and match similar, duplicate, or inconsistent company and organization names at scale.

These examples demonstrate how to generate and use **AI-powered similarity keys** inside Snowflake SQL scripts (using user-defined functions) to improve **data quality, entity resolution, deduplication, analytics accuracy, and provide the ability to join inconsistent company/organization names across tables**.

The combination of knowledge bases, specialized algorithms, and an AI-enhanced approach goes far beyond what can be achieved with "fuzzy matching" and other string-matching based approaches.


Learn more about the API:
https://www.interzoid.com/apis/company-name-matching

Website: https://www.interzoid.com

Also see:

Individual Name Matching with Snowflake: https://github.com/interzoid/interzoid-platform/tree/main/individual-name-matching/snowflake-examples

Street Address Matching with Snowflake: https://github.com/interzoid/interzoid-platform/tree/main/street-address-matching/snowflake-examples

You can also connect and run matching jobs from a browser app with this API using a connection string to access Snowflake tables:
https://snowflake-batch.interzoid.com/

---

## Overview

Company and organization names often appear in many different formats across systems due to abbreviations, legal suffix variations, misspellings, punctuation differences, and inconsistent capitalization.

The Interzoid Company Name Matching API analyzes each company name and then generates and returns a **similarity key**. Records that share the same similarity key are considered strong matches, even when the original name strings differ significantly.

By generating and storing similarity keys in Snowflake, you can use standard SQL to efficiently group, match, and analyze related company records.


---

## What This Directory Demonstrates

The Snowflake examples in this directory show how to:

- Generate company name similarity keys using Interzoid APIs
- Store similarity keys in Snowflake tables
- Identify duplicate or related company records using SQL
- Join/match records across tables while overcoming company inconsistency
- Create match reports for analytics and data cleansing
- Support entity resolution and master data management workflows

These patterns are suitable for both **ad-hoc analysis** and **automated ELT/ETL pipelines**.

Example results after running SQL script with similar company names generating the same similarity key, making identification or matching easy (insert-simkeys.sql):

![img.png](img.png)
---

### Files:

- example.sql - generates a similarity key for one company name
- insert-simkeys.sql - appends similarity keys for each row in a table
- match-report.sql - generates a report in SQL showing company names that share the same simkey in groups

## Common Company Name Matching Use Cases

Interzoid Company Name Matching is commonly used for:

### Data Quality and Deduplication
- Detect duplicate company records across multiple sources
- Normalize inconsistent organization names
- Improve accuracy of CRM, ERP, and customer databases

### Entity Resolution and Master Data
- Link records that refer to the same company using different names
- Support master data management (MDM) initiatives
- Consolidate fragmented business entities

### Analytics and Reporting
- Group related company records for accurate reporting
- Improve aggregation and rollups in BI dashboards
- Reduce noise caused by inconsistent naming

### ELT / ETL Pipelines
- Enrich incoming records with similarity keys during ingestion
- Automate matching using Snowflake Tasks and Streams
- Use similarity keys as join keys across datasets

---

## How it Works in Snowflake

1. **Load Company Name Data**  
   Run with company or organization names in either an existing or new Snowflake table.

2. **Generate Similarity Keys**  (Performed by Interzoid)

   Call the Interzoid Company Name Matching API from Snowflake using an external function, UDF, or stored procedure.

3. **Store the Similarity Key**  (Performed by Interzoid)

   Save the returned similarity key in a column such as `SIMKEY`.

4. **Match and Group Records**  
   Use standard SQL `GROUP BY`, `JOIN`, or `ORDER BY` operations on the similarity key to identify matches.

### Example "Match Report" Matching Pattern
(After simkeys have been added to organization/company name tables)

```sql
SELECT
  SIMKEY,
  COUNT(*) AS RECORD_COUNT
FROM COMPANY_TABLE
GROUP BY SIMKEY
HAVING COUNT(*) > 1;
```

