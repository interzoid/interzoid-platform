--------------------------------------------------------------------------------
-- Steps to Call Interzoid's Email Trust Score API
-- Product Page: https://www.interzoid.com/apis/email-trust-score
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
-- 4. PYTHON FUNCTION (Corrected URL)
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION YOUR_DB.PUBLIC.get_email_trust_score(email_address STRING)
RETURNS VARIANT
LANGUAGE PYTHON
RUNTIME_VERSION = 3.10
HANDLER = 'get_score'
EXTERNAL_ACCESS_INTEGRATIONS = (interzoid_api_integration)
SECRETS = ('license' = YOUR_DB.PUBLIC.interzoid_license_key)
PACKAGES = ('requests', 'snowflake-snowpark-python')
AS
$$
import requests
import _snowflake
import json

def get_score(email_address):
    license_key = _snowflake.get_generic_secret_string('license')
    
    # FIXED: Removed 'get' from the URL. 
    # The correct endpoint is 'https://api.interzoid.com/emailtrustscore'
    url = "https://api.interzoid.com/emailtrustscore"
    
    params = {
        "license": license_key,
        "lookup": email_address  # Parameter must be 'lookup'
    }
    
    try:
        response = requests.get(url, params=params, timeout=60)
        
        if response.status_code == 200:
            return response.json()
        else:
            # Return the error code so we can see it in the table if it fails
            return {"Score": "Error", "Reasoning": f"HTTP {response.status_code}"}
            
    except Exception as e:
        return {"Score": "Error", "Reasoning": str(e)}
$$;

--------------------------------------------------------------------------------
-- 5. PERMISSIONS
--------------------------------------------------------------------------------
GRANT USAGE ON INTEGRATION interzoid_api_integration TO ROLE SYSADMIN;
GRANT READ ON SECRET YOUR_DB.PUBLIC.interzoid_license_key TO ROLE SYSADMIN;
GRANT USAGE ON DATABASE YOUR_DB TO ROLE SYSADMIN;
GRANT USAGE ON SCHEMA YOUR_DB.PUBLIC TO ROLE SYSADMIN;
GRANT USAGE ON FUNCTION YOUR_DB.PUBLIC.get_email_trust_score(STRING) TO ROLE SYSADMIN;

--------------------------------------------------------------------------------
-- 6. TEST & EXECUTION
--------------------------------------------------------------------------------

-- A. Create a staging table for your input list
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.input_emails (email STRING);

INSERT INTO YOUR_DB.PUBLIC.input_emails VALUES 
    ('bobsmith@gmail.com'),
    ('maria.garcia@adobe.com'),
    ('info@landscapesupplyco.net'),
    ('x9qz772@tempmail.plus');

-- B. Create the Results Table
CREATE OR REPLACE TABLE YOUR_DB.PUBLIC.email_trust_results (
    Input_Email STRING,
    Score STRING,       
    Reasoning STRING    
);

-- C. Populate the Results Table
-- The JSON keys 'Score' and 'Reasoning' are Case Sensitive.
INSERT INTO YOUR_DB.PUBLIC.email_trust_results
SELECT 
    email,
    YOUR_DB.PUBLIC.get_email_trust_score(email):Score::STRING,
    YOUR_DB.PUBLIC.get_email_trust_score(email):Reasoning::STRING
FROM YOUR_DB.PUBLIC.input_emails;

-- D. View the Final Clean Data
SELECT * FROM YOUR_DB.PUBLIC.email_trust_results
ORDER BY Score DESC;