# Street Address Matching with Snowflake

This directory contains **Snowflake integration examples** for the **Interzoid Street Address Matching (Address Match Similarity Key) API**:
https://www.interzoid.com/apis/street-address-matching

These examples help data engineers, analytics teams, and platform architects generate **AI-powered similarity keys** for street addresses and use those keys inside Snowflake for **data quality, deduplication, entity resolution, and analytics accuracy**.

---

## Overview

Street addresses often appear in many different forms due to:

- abbreviations (St vs Street, Ave vs Avenue)
- punctuation and spacing differences
- unit designators (Suite, Ste, Apt, #)
- inconsistent capitalization
- data entry errors and formatting variations

The Interzoid Street Address Matching API returns a **hashed similarity key** for a street address. Addresses that generate the same similarity key are considered likely matches. Once stored in Snowflake, similarity keys can be used to match, cluster, sort, and search address data efficiently using standard SQL.

Product documentation (authoritative reference for this directory):
https://www.interzoid.com/apis/street-address-matching

---

## What This Directory Demonstrates

The Snowflake examples in this directory show how to:

- generate similarity keys for street addresses using the Interzoid API
- store similarity keys in Snowflake tables (for example: `SIMKEY`)
- identify duplicate or related address records using SQL clustering patterns
- create match reports for cleansing, analytics, and operational workflows
- automate enrichment for new records via Snowflake pipelines

---

## Common Street Address Matching Use Cases

### Data Quality and Deduplication
- identify duplicate address records in customer, vendor, and facility datasets
- reduce redundant shipping or billing addresses
- improve address consistency across operational systems

### Entity Resolution
- link records that represent the same physical location
- connect datasets using similarity keys instead of brittle string comparisons
- support location-level entity resolution workflows

### Analytics and Reporting
- group address records for rollups and segmentation by location
- reduce fragmentation caused by inconsistent address formatting
- improve KPI accuracy for logistics, service coverage, or customer analysis

### ELT / ETL Pipelines
- enrich incoming records with similarity keys during ingestion
- automate matching using Snowflake Tasks and Streams
- use similarity keys as join keys across datasets

---

## Best Practice Notes (From the Product Page)

This API is designed to match on **street address data** (not city/state/zip).

For higher match rates in many workflows, Interzoid recommends:
- generate a similarity key from the **street address**
- then append the address **zip code** to create a combined key

See the product page for details:
https://www.interzoid.com/apis/street-address-matching

---

## How It Works in Snowflake

1. Load your street address data into a Snowflake table (for example: an `ADDRESS` column).
2. Call the Interzoid API to generate a similarity key for each address.
3. Store the returned similarity key in a column (for example: `SIMKEY`).
4. Use SQL `GROUP BY`, `JOIN`, or `ORDER BY` operations on the similarity key to identify likely matches.

### Example SQL: Find Duplicate Clusters by Similarity Key

```sql
SELECT
  SIMKEY,
  COUNT(*) AS RECORD_COUNT
FROM ADDRESS_TABLE
GROUP BY SIMKEY
HAVING COUNT(*) > 1
ORDER BY RECORD_COUNT DESC;
