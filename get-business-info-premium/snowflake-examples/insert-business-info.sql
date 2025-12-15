--------------------------------------------------------------------------------
-- Steps to Call Interzoid's Business Info API
-- Product Page: https://www.interzoid.com/apis/get-business-info
-- Get API Key: https://www.interzoid.com/register-api-account
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
-- 4. PYTHON FUNCTION (The Logic)
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_business_info(lookup_value STRING)
RETURNS VARIANT
LANGUAGE PYTHON
RUNTIME_VERSION = 3.10
HANDLER = 'get_info'
EXTERNAL_ACCESS_INTEGRATIONS = (interzoid_api_integration)
SECRETS = ('license' = YOUR_DB.PUBLIC.interzoid_license_key)
PACKAGES = ('requests', 'snowflake-snowpark-python')
AS
$$
import requests
import _snowflake
import json

def get_info(lookup_value):
    license_key = _snowflake.get_generic_secret_string('license')
    
    url = "https://api.interzoid.com/getbusinessinfo"
    
    params = {
        "license": license_key,
        "lookup": lookup_value # This API uses 'lookup' (Company Name, Domain, or Email)
    }
    
    try:
        response = requests.get(url, params=params, timeout=60)
        
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
GRANT USAGE ON FUNCTION YOUR_DB.PUBLIC.get_business_info(STRING) TO ROLE SYSADMIN;

--------------------------------------------------------------------------------
-- 6. TEST & EXECUTION
--------------------------------------------------------------------------------

-- Test 1: Create a table with the requested companies
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.target_companies (lookup_val STRING);

INSERT INTO YOUR_DB.PUBLIC.target_companies VALUES 
    ('Cisco'),
    ('Microsoft'),
    ('BYD'),
    ('Orange Telecom');

-- Select specific fields from the JSON response
SELECT 
    lookup_val,
    YOUR_DB.PUBLIC.get_business_info(lookup_val) as full_response,
    -- Extract specific fields for easier reading
    full_response:CompanyName::STRING as official_name,
    full_response:CompanyDescription::STRING as description,
    full_response:Revenue::STRING as revenue,
    full_response:NumberEmployees::STRING as employees,
    full_response:CompanyLocation::STRING as hq_location
FROM YOUR_DB.PUBLIC.target_companies;