#!/usr/bin/env python3
"""
Simple Python example calling Interzoid's Get Full Name Match API.

This script:
  1. Builds the API URL with parameters.
  2. Calls the API over HTTPS.
  3. Parses the JSON response.
  4. Prints the similarity key and metadata.

Only the Python standard library is used.
"""

import urllib.request
import urllib.error
import json

# Replace this with your API key from: https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"


def main():
    """
    Main program logic.
    """

    # Example request URL. You can change the fullname parameter as needed.
    url = (
        "https://api.interzoid.com/getfullnamematch"
        f"?license={API_KEY}"
        "&fullname=James%20Johnston"
    )

    # Make the HTTP request.
    try:
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                print("API returned HTTP status:", response.status)
                return

            # Read the response and convert from bytes to string.
            body = response.read().decode("utf-8")

    except urllib.error.URLError as e:
        print("Error calling API:", e)
        return

    # Parse the JSON response into a dictionary.
    try:
        data = json.loads(body)
    except json.JSONDecodeError as e:
        print("Error parsing JSON:", e)
        print("Raw response was:")
        print(body)
        return

    # Print the fields returned by the API.
    print("Similarity Key:", data.get("SimKey"))
    print("Result Code:", data.get("Code"))
    print("Remaining Credits:", data.get("Credits"))


if __name__ == "__main__":
    main()
