import json
import urllib.parse
import urllib.request

# Replace this with your API key from https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"

# Input file containing one full name per line
INPUT_FILE_NAME = "sample-input-file.txt"


def call_fullname_match_api(full_name: str) -> str:
    """
    Calls Interzoid's getfullnamematch API for a single full name
    and returns the similarity key (SimKey) as a string.
    Returns an empty string if there is an error or no SimKey.
    """

    # URL-encode the full name so it is safe to send as a parameter
    name_param = urllib.parse.quote(full_name)

    api_url = (
        "https://api.interzoid.com/getfullnamematch"
        f"?license={API_KEY}"
        f"&fullname={name_param}"
    )

    try:
        with urllib.request.urlopen(api_url) as response:
            body = response.read().decode("utf-8")
    except Exception as e:
        print(f"Error calling API for '{full_name}': {e}")
        return ""

    try:
        data = json.loads(body)
    except json.JSONDecodeError:
        print(f"Error parsing JSON for '{full_name}'. Raw response was:\n{body}\n")
        return ""

    # Expected JSON: {"SimKey":"...","Code":"Success","Credits":"..."}
    simkey = data.get("SimKey", "")
    return simkey or ""


def main():
    # Each record holds the original input and its SimKey
    records = []

    # Read the input file line by line
    try:
        with open(INPUT_FILE_NAME, "r", encoding="utf-8") as f:
            for line in f:
                full_name = line.strip()

                # Skip blank lines
                if not full_name:
                    continue

                simkey = call_fullname_match_api(full_name)

                # Skip if no SimKey returned
                if not simkey:
                    continue

                records.append({"input": full_name, "simkey": simkey})
    except OSError as e:
        print(f"Error opening or reading input file: {e}")
        return

    if not records:
        print("No records with similarity keys found.")
        return

    # Sort strictly by SimKey so identical keys are adjacent
    records.sort(key=lambda r: r["simkey"])

    current_key = None
    cluster = []

    def print_cluster(c):
        # Only print clusters with two or more records
        if len(c) < 2:
            return
        for rec in c:
            print(f"{rec['input']},{rec['simkey']}")
        print()  # blank line between clusters

    # Walk through sorted records and build clusters by SimKey
    for rec in records:
        if rec["simkey"] != current_key:
            if cluster:
                print_cluster(cluster)
            current_key = rec["simkey"]
            cluster = [rec]
        else:
            cluster.append(rec)

    # Flush the final cluster
    if cluster:
        print_cluster(cluster)


if __name__ == "__main__":
    main()
