#!/usr/bin/env python3
"""
Simple batch example calling Interzoid's Get Full Name Match API.

This script:
  1. Reads one name per line from an input text file.
  2. URL-encodes each name.
  3. Calls the API for each input.
  4. Writes a CSV file with two columns: original value and SimKey.

Only the Python standard library is used.
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

    # Try opening input file for reading.
    try:
        in_file = open(INPUT_FILE_NAME, "r", encoding="utf-8")
    except OSError as e:
        print("Error opening input file:", e)
        return

    with in_file:
        # Try opening output CSV for writing.
        try:
            out_file = open(OUTPUT_FILE_NAME, "w", newline="", encoding="utf-8")
        except OSError as e:
            print("Error creating output file:", e)
            return

        with out_file:
            csv_writer = csv.writer(out_file)

            line_number = 0

            # Iterate over the input file line by line.
            for line in in_file:
                line_number += 1

                # Remove only trailing newline, not other spaces.
                original_value = line.rstrip("\n")

                # Skip empty lines (optional).
                if len(original_value) == 0:
                    continue

                # URL encode the name.
                name_param = urllib.parse.quote(original_value)

                # Build API URL.
                url = (
                    "https://api.interzoid.com/getfullnamematch"
                    f"?license={API_KEY}"
                    f"&fullname={name_param}"
                )

                # Call the API and read response.
                try:
                    with urllib.request.urlopen(url) as response:
                        if response.status != 200:
                            print(
                                f"API error on line {line_number} "
                                f"({original_value!r}): HTTP {response.status}"
                            )
                            csv_writer.writerow([original_value, ""])
                            continue

                        # Read raw body string.
                        body = response.read().decode("utf-8")

                except urllib.error.URLError as e:
                    print(
                        f"API error on line {line_number} "
                        f"({original_value!r}): {e}"
                    )
                    csv_writer.writerow([original_value, ""])
                    continue

                # Parse the JSON response.
                try:
                    data = json.loads(body)
                except json.JSONDecodeError as e:
                    print(
                        f"JSON parse error for {original_value!r}: {e}"
                    )
                    csv_writer.writerow([original_value, ""])
                    continue

                # Extract SimKey field from response.
                sim_key = data.get("SimKey", "")

                # Write a CSV row with original value + SimKey.
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
