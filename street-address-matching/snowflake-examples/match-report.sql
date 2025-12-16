--------------------------------------------------------------------------------
-- COMPLETE END-TO-END SCRIPT: STREET ADDRESS MATCHING
-- 1. Setup Environment
-- 2. Create Security Objects
-- 3. Create Python Function (Address Match)
-- 4. Create Data (Addresses)
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

-- [STEP 3] PYTHON FUNCTION (The Logic for Addresses)
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_address_match(address_string STRING)
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

def get_match(address_string):
    # Retrieve key securely
    license_key = _snowflake.get_generic_secret_string('license')

    # Endpoint for Street Address Matching
    url = "https://api.interzoid.com/getaddressmatchadvanced"

    params = {
        "license": license_key,
        "address": address_string,
        # 'wide' algorithm is generally better for matching; it ignores suite/unit numbers
        # to find matches even if the suite number is missing or different.
        "algorithm": "model-v3-wide"
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
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.raw_addresses (full_address STRING);

INSERT INTO YOUR_DB.PUBLIC.raw_addresses VALUES
                                             ('500 Main St'),
                                             ('500 Main Street'),
                                             ('500 Main St.'),
                                             ('100 Broadway Ave Suite 2'),
                                             ('100 Broadway Avenue #2'),
                                             ('100 Bdwy Ave Ste 2'),
                                             ('1234 First Avenue'),
                                             ('1234 1st Ave');

-- [STEP 5] THE AGGREGATION QUERY
-- 1. Calls the function to get SimKeys for addresses
-- 2. Groups by SimKey
-- 3. Filters for duplicates (groups > 1)
WITH calculated_keys AS (
    SELECT
        full_address,
        -- Generate the SimKey using the ADDRESS function
        YOUR_DB.PUBLIC.get_address_match(full_address):SimKey::STRING AS SIMKEY
    FROM YOUR_DB.PUBLIC.raw_addresses
)
SELECT
    SIMKEY,
    COUNT(*) AS RECORD_COUNT,
    -- List the specific addresses that matched this key
    ARRAY_AGG(full_address) AS MATCHED_ADDRESSES
FROM calculated_keys
GROUP BY SIMKEY
HAVING COUNT(*) > 1;