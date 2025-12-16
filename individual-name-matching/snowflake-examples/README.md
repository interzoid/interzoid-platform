# Individual Name Matching with Snowflake

This directory contains **Snowflake integration examples** for the **Interzoid Individual Name Matching (Full Name Match) API**:
https://www.interzoid.com/apis/individual-name-matching

These examples help data engineers, analytics teams, and platform architects generate **AI-powered similarity keys** for full names and use those keys inside Snowflake for **identity resolution, deduplication, data quality, and analytics accuracy**.

---

## Overview

Individual names frequently appear in many different forms due to:

- misspellings and typos
- nicknames and shortened names
- prefixes/suffixes (e.g., “Mr.”, “III”)
- punctuation and spacing differences
- international naming conventions and character sets

The Interzoid Individual Name Matching API returns a **hashed similarity key** for a full name. Full names that generate the same similarity key are considered likely matches. Once stored in Snowflake, similarity keys can be used to match, cluster, sort, and search name data efficiently using standard SQL.

Product documentation (authoritative reference for this directory):
https://www.interzoid.com/apis/individual-name-matching

---

## What This Directory Demonstrates

The Snowflake examples in this directory show how to:

- generate similarity keys for full names using the Interzoid API
- store similarity keys in Snowflake tables (e.g., `SIMKEY`)
- identify duplicates and near-duplicates using SQL clustering patterns
- create match reports for cleansing, analytics, and observability
- automate enrichment for new records via Snowflake pipelines

---

## Common Individual Name Matching Use Cases

### Data Quality and Deduplication
- detect duplicate customer/user/person records
- reduce redundant contacts across CRMs and operational systems
- clean up inconsistent naming formats in data lakes and warehouses

### Identity Resolution
- link records that refer to the same individual across systems
- support “customer 360” and unified identity initiatives
- improve match rates beyond basic string-distance fuzzy matching

### Analytics and Reporting
- group records by identity to improve counts, rollups, and segmentation
- reduce fragmentation caused by inconsistent name entry
- improve downstream model and KPI accuracy by clustering similar identities

### Search and Retrieval
- use similarity keys as a fast “fuzzy search mechanism” for names
- enable name-based clustering and lookups without storing raw name variations

---

## How It Works in Snowflake

1. Load your name data into a Snowflake table (for example: a `FULLNAME` column).
2. Call the Interzoid API to generate a similarity key for each name.
3. Store the returned similarity key in a column (for example: `SIMKEY`).
4. Use standard Snowflake SQL to group, join, and report on likely matches.

### Example SQL: Find Duplicate Clusters by Similarity Key

```sql
SELECT
  SIMKEY,
  COUNT(*) AS RECORD_COUNT
FROM INDIVIDUAL_TABLE
GROUP BY SIMKEY
HAVING COUNT(*) > 1
ORDER BY RECORD_COUNT DESC;
