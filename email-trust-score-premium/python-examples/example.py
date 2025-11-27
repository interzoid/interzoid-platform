#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Email Trust Score API.

This script:
  1. Encodes the lookup email address for URL safety.
  2. Calls the API over HTTPS.
  3. Parses the JSON response.
  4. Prints the returned values.

No third-party libraries required.
"""

import urllib.request
import urllib.parse
import urllib.error
import json

# Replace this with your API key from: https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"


def main():
    """
    Main entry point for the program.
    """

    # Email may contain characters needing URL encoding,
    # so we use urllib.parse.quote (same idea as QueryEscape in Go).
    email = "billsmith11@gmail.com"
    email_encoded = urllib.parse.quote(email)

    # Build the API URL
    url = (
        "https://api.interzoid.com/emailtrustscore"
        f"?license={API_KEY}"
        f"&lookup={email_encoded}"
    )

    # Make the API request
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read the raw bytes and convert to text
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        print("Error calling API:", e)
        return

    # Convert JSON string into Python dictionary
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Extract and print values just like the Go version
    print("Email:", data.get("Email"))
    print("Trust Score:", data.get("Score"))
    print("Reasoning:", data.get("Reasoning"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
