--------------------------------------------------------------------------------
-- COMPLETE END-TO-END SCRIPT: INDIVIDUAL NAME MATCHING
-- 1. Setup Environment
-- 2. Create Security Objects
-- 3. Create Python Function (Individual Match)
-- 4. Create Data (People)
-- 5. Run Aggregation Query
--------------------------------------------------------------------------------

-- [STEP 1] ENVIRONMENT SETUP
USE ROLE ACCOUNTADMIN;
CREATE OR REPLACE DATABASE YOUR_DB;
USE DATABASE YOUR_DB;
USE SCHEMA PUBLIC;
USE WAREHOUSE COMPUTE_WH;

-- [STEP 2] SECURITY OBJECTS
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

-- [STEP 3] PYTHON FUNCTION (The Logic for Individuals)
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_individual_match(fullname STRING)
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

def get_match(fullname):
    # Retrieve key securely
    license_key = _snowflake.get_generic_secret_string('license')

    # Endpoint for Individual Name Matching
    url = "https://api.interzoid.com/getfullnamematch"

    params = {
        "license": license_key,
        "fullname": fullname
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
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.raw_people (full_name STRING);

INSERT INTO YOUR_DB.PUBLIC.raw_people VALUES
                                          ('Jim Kelly'),
                                          ('James Kelley'),
                                          ('Allie Mcbeel'),
                                          ('Allisan Mcbiele'),
                                          ('Gerhard Schroeder'),
                                          ('Herr Geahardt Shroder'),
                                          ('Wanda Jackson'),
                                          ('Yousef Goldstein'),
                                          ('Yosif Gouldstine'),
                                          ('Mr Jim H Kellie'),
                                          ('Allison Mcbeal');

-- [STEP 5] THE AGGREGATION QUERY
-- 1. Calls the function to get SimKeys for people
-- 2. Groups by SimKey
-- 3. Filters for duplicates (groups > 1)
WITH calculated_keys AS (
    SELECT
        full_name,
        -- Generate the SimKey using the INDIVIDUAL function
        YOUR_DB.PUBLIC.get_individual_match(full_name):SimKey::STRING AS SIMKEY
    FROM YOUR_DB.PUBLIC.raw_people
)
SELECT
    SIMKEY,
    COUNT(*) AS RECORD_COUNT,
    -- List the specific names that matched this key
    ARRAY_AGG(full_name) AS MATCHED_NAMES
FROM calculated_keys
GROUP BY SIMKEY
HAVING COUNT(*) > 1;