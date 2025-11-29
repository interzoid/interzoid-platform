#!/usr/bin/env python3
"""
Simple Python batch example calling Interzoid's Get Company Match Advanced API.

This script:
  1. Reads company names from a text file (one per line).
  2. Calls the API for each company to obtain a similarity key (SimKey).
  3. Writes a CSV file with two columns: original value and SimKey.

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

    # Open the input file for reading (text mode).
    try:
        in_file = open(INPUT_FILE_NAME, "r", encoding="utf-8")
    except OSError as e:
        print("Error opening input file:", e)
        return

    # Ensure the file is closed when we are finished.
    with in_file:
        # Open the output file for writing CSV data.
        # newline='' is recommended when using csv.writer in Python.
        try:
            out_file = open(OUTPUT_FILE_NAME, "w", newline="", encoding="utf-8")
        except OSError as e:
            print("Error creating output file:", e)
            return

        with out_file:
            csv_writer = csv.writer(out_file)

            line_number = 0

            # Read the input file line-by-line.
            for line in in_file:
                line_number += 1

                # Remove only the trailing newline; preserve other spaces.
                original_value = line.rstrip("\n")

                # Skip completely empty lines (optional).
                if len(original_value) == 0:
                    continue

                # URL-encode the company name so it is safe in the query string.
                company_param = urllib.parse.quote(original_value)

                # Build the API URL for this company.
                url = (
                    "https://api.interzoid.com/getcompanymatchadvanced"
                    f"?license={API_KEY}"
                    f"&company={company_param}"
                    "&algorithm=model-v4-wide"
                )

                # Call the API.
                try:
                    with urllib.request.urlopen(url) as response:
                        if response.status != 200:
                            print(
                                f"Error calling API for line {line_number} "
                                f"({original_value!r}): HTTP {response.status}"
                            )
                            # Write row with empty SimKey so CSV still lines up.
                            csv_writer.writerow([original_value, ""])
                            continue

                        # Read and decode the response body.
                        body = response.read().decode("utf-8")

                except urllib.error.URLError as e:
                    print(
                        f"Error calling API for line {line_number} "
                        f"({original_value!r}): {e}"
                    )
                    csv_writer.writerow([original_value, ""])
                    continue

                # Parse the JSON response.
                try:
                    data = json.loads(body)
                except json.JSONDecodeError as e:
                    print(
                        f"Error parsing JSON for line {line_number} "
                        f"({original_value!r}): {e}"
                    )
                    csv_writer.writerow([original_value, ""])
                    continue

                # Optional: check the result code.
                code = data.get("Code")
                if code != "Success":
                    print(
                        f"Non-success code for line {line_number} "
                        f"({original_value!r}): Code={code}"
                    )

                # Extract SimKey (may be None if missing).
                sim_key = data.get("SimKey", "")

                # Write the original value and SimKey as a CSV row.
                # csv.writer will handle quoting for commas or quotes.
                try:
                    csv_writer.writerow([original_value, sim_key])
                except OSError as e:
                    print(
                        f"Error writing CSV row for line {line_number} "
                        f"({original_value!r}): {e}"
                    )
                    continue

    print("Done. Results written to", OUTPUT_FILE_NAME)


if __name__ == "__main__":
    main()
