#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Parent Company Info API.

This script:
  1. URL-encodes the lookup value.
  2. Calls the API over HTTPS.
  3. Parses the JSON response.
  4. Prints selected fields.

Only the Python standard library is used.
"""

import urllib.request
import urllib.parse
import urllib.error
import json

# Replace this with your API key from: https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"


def main():
    """
    Main program logic.
    """

    # The lookup value may contain characters that need encoding,
    # so we use urllib.parse.quote for URL safety.
    lookup = "informatica"
    lookup_encoded = urllib.parse.quote(lookup)

    # Build the full URL with parameters.
    url = (
        "https://api.interzoid.com/getparentcompanyinfo"
        f"?license={API_KEY}"
        f"&lookup={lookup_encoded}"
    )

    # Make the API request.
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read and decode the response text.
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        print("Error calling API:", e)
        return

    # Parse the JSON response.
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Print the fields returned by the API.
    print("Company Name:", data.get("CompanyName"))
    print("Company URL:", data.get("CompanyURL"))
    print("Parent Company:", data.get("ParentCompany"))
    print("Parent Location:", data.get("ParentCompanyLocation"))
    print("Parent Website:", data.get("ParentCompanyURL"))
    print("Parent Description:", data.get("ParentCompanyDescription"))
    print("Parent Reference URL:", data.get("ParentCompanyReferenceURL"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
