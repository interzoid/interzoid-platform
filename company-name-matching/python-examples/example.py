#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Company Match Advanced API.

This script:
  1. Builds the API URL with your API key and parameters.
  2. Calls the API over HTTPS.
  3. Parses the JSON response.
  4. Prints the similarity key, result code, and remaining credits.

No third-party libraries are required.
"""

import json
import urllib.request
import urllib.error

# Replace this with your API key from: https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"


def main():
    """
    Main function that performs the API call and prints the results.
    """

    # In this simple example we hard-code the endpoint parameters:
    # - company: the company name to match ("ibm" here)
    # - algorithm: the matching algorithm type
    company = "ibm"
    algorithm = "model-v4-wide"

    # Build the full URL with query parameters.
    # You can change `company` and `algorithm` as needed.
    url = (
        "https://api.interzoid.com/getcompanymatchadvanced"
        f"?license={API_KEY}"
        f"&company={company}"
        f"&algorithm={algorithm}"
    )

    # Call the API using the built-in urllib.request module.
    try:
        with urllib.request.urlopen(url) as response:
            # Optionally, check the HTTP status code.
            if response.status != 200:
                print("Error calling API. HTTP status code:", response.status)
                return

            # Read the raw response body (bytes) and decode to string.
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        # This catches network-related errors (DNS failure, refused connection, etc.)
        print("Error calling API:", e)
        return

    # Parse the JSON response into a Python dictionary.
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Extract fields from the JSON response.
    # These correspond to the same fields used in the Go example.
    sim_key = data.get("SimKey")
    code = data.get("Code")
    credits = data.get("Credits")

    # Print the results to the console.
    print("Match Similarity Key:", sim_key)
    print("Result Code:", code)
    print("Remaining Credits:", credits)


# This ensures main() runs only when the script is executed directly,
# and not when it is imported as a module.
if __name__ == "__main__":
    main()
