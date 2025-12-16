--------------------------------------------------------------------------------
-- COMPLETE END-TO-END SCRIPT
-- 1. Setup Environment
-- 2. Create Security Objects
-- 3. Create Python Function
-- 4. Create Data
-- 5. Run Matching Query
--------------------------------------------------------------------------------

-- [STEP 1] ENVIRONMENT SETUP
USE ROLE ACCOUNTADMIN;
CREATE OR REPLACE DATABASE YOUR_DB;
USE DATABASE YOUR_DB;
USE SCHEMA PUBLIC;
USE WAREHOUSE COMPUTE_WH;

-- [STEP 2] SECURITY OBJECTS (Network Rule & Secret)
-- Whitelist the API Domain
CREATE OR REPLACE NETWORK RULE YOUR_DB.PUBLIC.interzoid_api_rule
  MODE = EGRESS
  TYPE = HOST_PORT
  VALUE_LIST = ('api.interzoid.com');

-- Securely store the License Key
CREATE OR REPLACE SECRET YOUR_DB.PUBLIC.interzoid_license_key
  TYPE = GENERIC_STRING
  SECRET_STRING = 'your-api-key'; -- <--- PASTE YOUR ACTUAL KEY HERE !!!

-- Connect Rule and Secret via Integration
CREATE OR REPLACE EXTERNAL ACCESS INTEGRATION interzoid_api_integration
  ALLOWED_NETWORK_RULES = (YOUR_DB.PUBLIC.interzoid_api_rule)
  ALLOWED_AUTHENTICATION_SECRETS = (YOUR_DB.PUBLIC.interzoid_license_key)
  ENABLED = TRUE;

-- [STEP 3] PYTHON FUNCTION (The Logic)
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_company_match(company_name STRING)
RETURNS VARIANT
LANGUAGE PYTHON
RUNTIME_VERSION = 3.10
HANDLER = 'get_match'
EXTERNAL_ACCESS_INTEGRATIONS = (interzoid_api_integration)
SECRETS = ('license' = YOUR_DB.PUBLIC.interzoid_license_key)
PACKAGES = ('requests', 'snowflake-snowpark-python')
AS
$$
import requests
import _snowflake
import json

def get_match(company_name):
    # Retrieve key securely
    license_key = _snowflake.get_generic_secret_string('license')

    url = "https://api.interzoid.com/getcompanymatchadvanced"

    params = {
        "license": license_key,
        "company": company_name,
        "algorithm": "model-v4-wide"
    }

    try:
        response = requests.get(url, params=params, timeout=10)

        if response.status_code == 200:
            return response.json()
        else:
            return {"Code": "Error", "Message": f"HTTP {response.status_code}"}

    except Exception as e:
        return {"Code": "Error", "Message": str(e)}
$$;

-- [STEP 4] CREATE TABLE AND DUMMY DATA
-- We create 'raw_companies' instead of 'COMPANY_TABLE' to ensure it matches
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.raw_companies (company_name STRING);

INSERT INTO YOUR_DB.PUBLIC.raw_companies VALUES
                                             ('IBM'),
                                             ('Microsoft Corp'),
                                             ('microsot'),
                                             ('Amazon'),
                                             ('Amazon.com'),
                                             ('Google Inc');

-- [STEP 5] THE AGGREGATION QUERY (The Adapted SQL)
-- 1. Calls the function to get SimKeys
-- 2. Groups by SimKey
-- 3. Filters for duplicates
WITH calculated_keys AS (
    SELECT
        company_name,
        -- Call the function from [STEP 3]
        YOUR_DB.PUBLIC.get_company_match(company_name):SimKey::STRING AS SIMKEY
    FROM YOUR_DB.PUBLIC.raw_companies
)
SELECT
    SIMKEY,
    COUNT(*) AS RECORD_COUNT,
    ARRAY_AGG(company_name) AS MATCHED_NAMES
FROM calculated_keys
GROUP BY SIMKEY
HAVING COUNT(*) > 1;