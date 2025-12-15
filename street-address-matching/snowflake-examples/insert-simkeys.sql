--------------------------------------------------------------------------------
-- Steps to Call Interzoid's Street Address Matching API
-- Product Page: https://www.interzoid.com/apis/street-address-matching
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
    # Retrieve key securely using the key 'license' defined in SECRETS above
    license_key = _snowflake.get_generic_secret_string('license')
    
    # Updated Endpoint for Street Address Matching
    url = "https://api.interzoid.com/getaddressmatchadvanced"
    
    params = {
        "license": license_key,
        "address": address_string,
        "algorithm": "model-v3-wide"  # Use 'wide' to ignore unit numbers, 'narrow' to include them
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
GRANT USAGE ON FUNCTION YOUR_DB.PUBLIC.get_address_match(STRING) TO ROLE SYSADMIN;

--------------------------------------------------------------------------------
-- 6. TEST & EXECUTION
--------------------------------------------------------------------------------

-- Test 1: Create a dummy table with address variations
-- We explicitly specify YOUR_DB.PUBLIC so it works even if no DB is selected.

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

SELECT 
    full_address,
    -- Use the full path to the function
    YOUR_DB.PUBLIC.get_address_match(full_address):SimKey::STRING as sim_key
FROM YOUR_DB.PUBLIC.raw_addresses
ORDER BY sim_key;