#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Custom Data Enrichment API.

This script:
  1. Builds the API URL with URL-encoded parameters.
  2. Calls the API using standard Python.
  3. Parses the JSON response.
  4. Prints the individual enrichment fields.

No external libraries required.
"""

import urllib.request
import urllib.parse
import urllib.error
import json

# Replace this with your API key from: https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"


def main():
    """
    Main entry point of the code.
    """

    # These are the example parameters. They must be URL-encoded
    # because some characters are not safe in a URL.
    topic = "detailed information about companies"
    lookup = "IBM"
    output = '["headquarters", "ceo", "website","number of employees","ticker symbol","2023 revenue","2022 revenue"]'

    # URL-encode each parameter, similar to Go's url.QueryEscape()
    topic_encoded = urllib.parse.quote(topic)
    lookup_encoded = urllib.parse.quote(lookup)
    output_encoded = urllib.parse.quote(output)

    # Build the API URL
    url = (
        "https://api.interzoid.com/getcustom"
        f"?license={API_KEY}"
        f"&topic={topic_encoded}"
        f"&lookup={lookup_encoded}"
        "&model=default"
        f"&output={output_encoded}"
    )

    # Make the API request
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read the raw bytes and decode as UTF-8
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

    # Extract the fields exactly like the Go struct
    print("Headquarters:", data.get("headquarters"))
    print("CEO:", data.get("ceo"))
    print("Website:", data.get("website"))
    print("Employees:", data.get("number of employees"))
    print("Ticker Symbol:", data.get("ticker symbol"))
    print("2023 Revenue:", data.get("2023 revenue"))
    print("2022 Revenue:", data.get("2022 revenue"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
