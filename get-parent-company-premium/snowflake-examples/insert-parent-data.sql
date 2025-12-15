--------------------------------------------------------------------------------
-- Steps to Call Interzoid's Parent Company Info API
-- Product Page: https://www.interzoid.com/apis/get-parent-company-info
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
-- 4. PYTHON FUNCTION (Updated Timeout)
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_parent_info(company_name STRING)
RETURNS VARIANT
LANGUAGE PYTHON
RUNTIME_VERSION = 3.10
HANDLER = 'get_parent'
EXTERNAL_ACCESS_INTEGRATIONS = (interzoid_api_integration)
SECRETS = ('license' = YOUR_DB.PUBLIC.interzoid_license_key)
PACKAGES = ('requests', 'snowflake-snowpark-python')
AS
$$
import requests
import _snowflake
import json

def get_parent(company_name):
    license_key = _snowflake.get_generic_secret_string('license')
    
    url = "https://api.interzoid.com/getparentcompanyinfo"
    
    params = {
        "license": license_key,
        "lookup": company_name 
    }
    
    try:
        # TIMEOUT UPDATED TO 60 SECONDS
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
GRANT USAGE ON FUNCTION YOUR_DB.PUBLIC.get_parent_info(STRING) TO ROLE SYSADMIN;

--------------------------------------------------------------------------------
-- 6. TEST & EXECUTION
--------------------------------------------------------------------------------

-- A. Create a staging table for your input list
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.input_companies (lookup_val STRING);

INSERT INTO YOUR_DB.PUBLIC.input_companies VALUES 
    ('Informatica'),
    ('Ampere Computing'),
    ('Dotmatics');

-- B. Create the Results Table
-- The columns match the API output parameters. We do NOT include a JSON column.
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.parent_company_results (
    Input_Company STRING,
    ParentCompany STRING,
    ParentCompanyLocation STRING,
    ParentCompanyURL STRING,
    ParentCompanyDescription STRING
);

-- C. Populate the Results Table
-- We call the API and parse the JSON immediately during the INSERT.
INSERT INTO YOUR_DB.PUBLIC.parent_company_results
SELECT 
    lookup_val,
    YOUR_DB.PUBLIC.get_parent_info(lookup_val):ParentCompany::STRING,
    YOUR_DB.PUBLIC.get_parent_info(lookup_val):ParentCompanyLocation::STRING,
    YOUR_DB.PUBLIC.get_parent_info(lookup_val):ParentCompanyURL::STRING,
    YOUR_DB.PUBLIC.get_parent_info(lookup_val):ParentCompanyDescription::STRING
FROM YOUR_DB.PUBLIC.input_companies;

-- D. View the Final Clean Data
SELECT * FROM YOUR_DB.PUBLIC.parent_company_results;