--------------------------------------------------------------------------------
-- Steps to Call Interzoid's Organization Name Standardization API
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- 1. ENVIRONMENT SETUP
--------------------------------------------------------------------------------
USE ROLE ACCOUNTADMIN;
USE WAREHOUSE COMPUTE_WH;

CREATE OR REPLACE DATABASE YOUR_DB;
USE DATABASE YOUR_DB;
USE SCHEMA PUBLIC;

--------------------------------------------------------------------------------
-- 2. SECURITY OBJECTS
--------------------------------------------------------------------------------
CREATE OR REPLACE NETWORK RULE YOUR_DB.PUBLIC.interzoid_api_rule
  MODE = EGRESS
  TYPE = HOST_PORT
  VALUE_LIST = ('api.interzoid.com');

CREATE OR REPLACE SECRET YOUR_DB.PUBLIC.interzoid_license_key
  TYPE = GENERIC_STRING
  SECRET_STRING = 'your-api-key'; -- <--- PASTE YOUR ACTUAL KEY HERE

--------------------------------------------------------------------------------
-- 3. INTEGRATION
--------------------------------------------------------------------------------
CREATE OR REPLACE EXTERNAL ACCESS INTEGRATION interzoid_api_integration
  ALLOWED_NETWORK_RULES = (YOUR_DB.PUBLIC.interzoid_api_rule)
  ALLOWED_AUTHENTICATION_SECRETS = (YOUR_DB.PUBLIC.interzoid_license_key)
  ENABLED = TRUE;

--------------------------------------------------------------------------------
-- 4. PYTHON FUNCTION (Corrected)
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_org_standard(company_name STRING)
RETURNS VARIANT
LANGUAGE PYTHON
RUNTIME_VERSION = 3.10
HANDLER = 'get_std'
EXTERNAL_ACCESS_INTEGRATIONS = (interzoid_api_integration)
SECRETS = ('license' = YOUR_DB.PUBLIC.interzoid_license_key)
PACKAGES = ('requests', 'snowflake-snowpark-python')
AS
$$
import requests
import _snowflake
import json

def get_std(company_name):
    license_key = _snowflake.get_generic_secret_string('license')
    
    url = "https://api.interzoid.com/getorgstandard"
    
    params = {
        "license": license_key,
        "org": company_name   # FIXED: Parameter must be 'org', not 'company'
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

--------------------------------------------------------------------------------
-- 5. PERMISSIONS
--------------------------------------------------------------------------------
GRANT USAGE ON INTEGRATION interzoid_api_integration TO ROLE SYSADMIN;
GRANT READ ON SECRET YOUR_DB.PUBLIC.interzoid_license_key TO ROLE SYSADMIN;
GRANT USAGE ON DATABASE YOUR_DB TO ROLE SYSADMIN;
GRANT USAGE ON SCHEMA YOUR_DB.PUBLIC TO ROLE SYSADMIN;
GRANT USAGE ON FUNCTION YOUR_DB.PUBLIC.get_org_standard(STRING) TO ROLE SYSADMIN;

--------------------------------------------------------------------------------
-- 6. TEST & EXECUTION (Corrected Extraction)
--------------------------------------------------------------------------------

CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.raw_companies_std (company_name STRING);

INSERT INTO YOUR_DB.PUBLIC.raw_companies_std VALUES 
    ('Amazon.com Inc.'),
    ('Amazon, Inc.'),
    ('Amazon Incorporated'),
    ('International Business Machines'),
    ('IBM Corp.'),
    ('Ford Motor Company'),
    ('Ford Motors');

SELECT 
    company_name as original_name,
    -- FIXED: Key is 'Standard' (Case Sensitive)
    YOUR_DB.PUBLIC.get_org_standard(company_name):Standard::STRING as standard
FROM YOUR_DB.PUBLIC.raw_companies_std;