#!/usr/bin/env python3
"""
Simple batch example calling Interzoid's Get Address Match Advanced API.

This script:
  1. Reads one address per line from an input text file.
  2. URL-encodes the address.
  3. Calls the API for each address.
  4. Writes a CSV file with original value and SimKey.

Only Python's standard library is used.
"""

import csv
import json
import urllib.request
import urllib.parse
import urllib.error

# Replace this with your API key from: https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"

# Hardcoded input and output file names
INPUT_FILE_NAME = "sample-input-file.txt"
OUTPUT_FILE_NAME = "output.csv"


def main():
    """
    Main program logic.
    """

    # Open input file for reading
    try:
        in_file = open(INPUT_FILE_NAME, "r", encoding="utf-8")
    except OSError as e:
        print("Error opening input file:", e)
        return

    with in_file:
        # Open output CSV file for writing
        try:
            out_file = open(OUTPUT_FILE_NAME, "w", newline="", encoding="utf-8")
        except OSError as e:
            print("Error creating output file:", e)
            return

        with out_file:
            csv_writer = csv.writer(out_file)
            line_number = 0

            # Iterate line by line
            for line in in_file:
                line_number += 1

                # Remove trailing newline only
                original_value = line.rstrip("\n")

                # skip blank lines if desired
                if len(original_value) == 0:
                    continue

                # URL-encode the address
                address_param = urllib.parse.quote(original_value)

                # Build API endpoint
                url = (
                    "https://api.interzoid.com/getaddressmatchadvanced"
                    f"?license={API_KEY}"
                    f"&address={address_param}"
                    "&algorithm=model-v3-narrow"
                )

                # Call the API
                try:
                    with urllib.request.urlopen(url) as response:
                        if response.status != 200:
                            print(
                                f"Error calling API for line {line_number} "
                                f"({original_value!r}): HTTP {response.status}"
                            )
                            csv_writer.writerow([original_value, ""])
                            continue

                        # Read and decode response
                        body = response.read().decode("utf-8")

                except urllib.error.URLError as e:
                    print(
                        f"Error calling API for line {line_number} "
                        f"({original_value!r}): {e}"
                    )
                    csv_writer.writerow([original_value, ""])
                    continue

                # Parse JSON into dict
                try:
                    data = json.loads(body)
                except json.JSONDecodeError as e:
                    print(
                        f"Error parsing JSON for {original_value!r}: {e}"
                    )
                    csv_writer.writerow([original_value, ""])
                    continue

                # Extract SimKey (may not always be present)
                sim_key = data.get("SimKey", "")

                # Write row to CSV
                try:
                    csv_writer.writerow([original_value, sim_key])
                except OSError as e:
                    print(
                        f"CSV write error for {original_value!r}: {e}"
                    )
                    continue

    print("Done. Results written to", OUTPUT_FILE_NAME)


if __name__ == "__main__":
    main()
