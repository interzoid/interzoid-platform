#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Business Info API.

This script:
  1. URL-encodes the lookup value.
  2. Calls the API using only the standard library.
  3. Parses the JSON.
  4. Prints the resulting fields.

No external dependencies required.
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
    # so we use urllib.parse.quote (same idea as url.QueryEscape in Go).
    lookup = "Cisco"
    lookup_encoded = urllib.parse.quote(lookup)

    # Build the API URL
    url = (
        "https://api.interzoid.com/getbusinessinfo"
        f"?license={API_KEY}"
        f"&lookup={lookup_encoded}"
    )

    # Perform the HTTP GET request
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read and decode the raw byte response into a UTF-8 string
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        print("Error calling API:", e)
        return

    # Parse JSON into a Python dictionary
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Print the values just like the Go version
    print("Company Name:", data.get("CompanyName"))
    print("Website:", data.get("CompanyURL"))
    print("Location:", data.get("CompanyLocation"))
    print("Description:", data.get("CompanyDescription"))
    print("Revenue:", data.get("Revenue"))
    print("Employees:", data.get("NumberEmployees"))
    print("NAICS:", data.get("NAICS"))
    print("Top Executive:", data.get("TopExecutive"))
    print("Top Executive Title:", data.get("TopExecutiveTitle"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
