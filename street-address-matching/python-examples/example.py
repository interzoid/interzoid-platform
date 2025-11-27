#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Address Match Advanced API.

This script:
  1. URL-encodes the address parameter.
  2. Calls the API over HTTPS.
  3. Parses the JSON response.
  4. Prints the similarity key and metadata.

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

    # Address may contain spaces and punctuation,
    # so we URL-encode it for safe usage in the query string.
    address = "400 East Broadway St"
    address_encoded = urllib.parse.quote(address)

    # Build the full API URL with parameters
    url = (
        "https://api.interzoid.com/getaddressmatchadvanced"
        f"?license={API_KEY}"
        f"&address={address_encoded}"
        "&algorithm=model-v3-narrow"
    )

    # Make the HTTP request
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read and decode the response body
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        print("Error calling API:", e)
        return

    # Parse the JSON into a Python dictionary
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Print the fields from the response
    print("Address Similarity Key:", data.get("SimKey"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
