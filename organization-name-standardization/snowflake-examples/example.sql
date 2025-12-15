--------------------------------------------------------------------------------
-- Steps to Call Interzoid's Organization Name Standardization API
-- Product Page: https://www.interzoid.com/apis/get-org-standard
-- Get API Key: https://www.interzoid.com/register-api-account
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- 1. ENVIRONMENT SETUP
--------------------------------------------------------------------------------
-- Use the most powerful role to ensure we have permission to create Integrations
USE ROLE ACCOUNTADMIN;
USE WAREHOUSE COMPUTE_WH; -- Ensure you have a warehouse selected

-- Create the new database and set context
CREATE OR REPLACE DATABASE YOUR_DB;
USE DATABASE YOUR_DB;
USE SCHEMA PUBLIC;

--------------------------------------------------------------------------------
-- 2. SECURITY OBJECTS (Rule & Secret)
--------------------------------------------------------------------------------
-- Whitelist the API Domain
CREATE OR REPLACE NETWORK RULE YOUR_DB.PUBLIC.interzoid_api_rule
  MODE = EGRESS
  TYPE = HOST_PORT
  VALUE_LIST = ('api.interzoid.com');

-- Securely store the License Key
CREATE OR REPLACE SECRET YOUR_DB.PUBLIC.interzoid_license_key
  TYPE = GENERIC_STRING
  SECRET_STRING = 'your-api-key'; -- <--- PASTE YOUR ACTUAL KEY HERE

--------------------------------------------------------------------------------
-- 3. INTEGRATION (The Bridge)
--------------------------------------------------------------------------------
-- Connect the Network Rule and Secret to the Account
CREATE OR REPLACE EXTERNAL ACCESS INTEGRATION interzoid_api_integration
  ALLOWED_NETWORK_RULES = (YOUR_DB.PUBLIC.interzoid_api_rule)
  ALLOWED_AUTHENTICATION_SECRETS = (YOUR_DB.PUBLIC.interzoid_license_key)
  ENABLED = TRUE;

--------------------------------------------------------------------------------
-- 4. PYTHON FUNCTION (The Logic)
--------------------------------------------------------------------------------
-- We use the fully qualified name (YOUR_DB.PUBLIC...) to avoid context errors
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_org_standard(org_name STRING)
RETURNS VARIANT
LANGUAGE PYTHON
RUNTIME_VERSION = 3.10
HANDLER = 'get_standard'
EXTERNAL_ACCESS_INTEGRATIONS = (interzoid_api_integration)
SECRETS = ('license' = YOUR_DB.PUBLIC.interzoid_license_key)
PACKAGES = ('requests', 'snowflake-snowpark-python')
AS
$$
import requests
import _snowflake
import json

def get_standard(org_name):
    # Retrieve key securely using the key 'license' defined in SECRETS above
    license_key = _snowflake.get_generic_secret_string('license')
    
    # Updated Endpoint for Organization Standardization
    url = "https://api.interzoid.com/getorgstandard"
    
    # Updated Parameters for Organization Standardization
    params = {
        "license": license_key,
        "org": org_name
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
-- 5. PERMISSIONS (Handover to Developer)
--------------------------------------------------------------------------------
-- Allow SYSADMIN to use what we just built
GRANT USAGE ON INTEGRATION interzoid_api_integration TO ROLE SYSADMIN;
GRANT READ ON SECRET YOUR_DB.PUBLIC.interzoid_license_key TO ROLE SYSADMIN;
GRANT USAGE ON DATABASE YOUR_DB TO ROLE SYSADMIN;
GRANT USAGE ON SCHEMA YOUR_DB.PUBLIC TO ROLE SYSADMIN;
GRANT USAGE ON FUNCTION YOUR_DB.PUBLIC.get_org_standard(STRING) TO ROLE SYSADMIN;

--------------------------------------------------------------------------------
-- 6. TEST & EXECUTION
--------------------------------------------------------------------------------

-- Test 1: Single Call
SELECT YOUR_DB.PUBLIC.get_org_standard('Intl Business Machines Corp');