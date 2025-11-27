#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Organization Standard API.

This script:
  1. URL-encodes the organization input.
  2. Calls the API over HTTPS.
  3. Parses the JSON response.
  4. Prints the standardized organization name.

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

    # The organization name may contain punctuation or spaces,
    # so we URL-encode it for safety.
    org = "b.o.a."
    org_encoded = urllib.parse.quote(org)

    # Build the API URL
    url = (
        "https://api.interzoid.com/getorgstandard"
        f"?license={API_KEY}"
        f"&org={org_encoded}"
    )

    # Make the HTTP request
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read the response and decode it
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        print("Error calling API:", e)
        return

    # Parse the JSON response
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Print the fields returned by the API
    print("Standardized Organization:", data.get("Standard"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
