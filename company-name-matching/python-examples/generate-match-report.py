import json
import urllib.parse
import urllib.request

# Replace this with your API key from https://www.interzoid.com/manage-api-account
API_KEY = "YOUR_API_KEY_HERE"

# Input file containing one company name per line
INPUT_FILE_NAME = "sample-input-file.txt"


def call_company_match_api(company_name: str) -> str:
    """
    Calls Interzoid's getcompanymatchadvanced API for a single company name
    and returns the similarity key (SimKey) as a string.
    Returns an empty string if there is an error or no SimKey.
    """

    # URL-encode the company name so it is safe to send as a URL parameter
    company_param = urllib.parse.quote(company_name)

    # Build the API URL
    api_url = (
        "https://api.interzoid.com/getcompanymatchadvanced"
        f"?license={API_KEY}"
        f"&company={company_param}"
        "&algorithm=model-v4-wide"
    )

    try:
        with urllib.request.urlopen(api_url) as response:
            body = response.read().decode("utf-8")
    except Exception as e:
        print(f"Error calling API for '{company_name}': {e}")
        return ""

    try:
        data = json.loads(body)
    except json.JSONDecodeError:
        print(f"Error parsing JSON for '{company_name}'. Raw response was:\n{body}\n")
        return ""

    # Expecting a JSON object like: {"SimKey": "...", "Code": "Success", "Credits": "..."}
    simkey = data.get("SimKey", "")
    return simkey or ""


def main():
    # Each record holds an input company name and its generated similarity key (SimKey)
    records = []

    # Read the input file line by line
    try:
        with open(INPUT_FILE_NAME, "r", encoding="utf-8") as f:
            for line in f:
                company = line.strip()

                # Skip blank lines
                if not company:
                    continue

                simkey = call_company_match_api(company)

                # If SimKey is empty, skip this record
                if not simkey:
                    continue

                records.append({"input": company, "simkey": simkey})
    except OSError as e:
        print(f"Error opening or reading input file: {e}")
        return

    if not records:
        print("No records with similarity keys found.")
        return

    # ----------------------------------------------------------------------
    # Sort the records strictly by SimKey only.
    # This ensures that all identical SimKeys are adjacent in the list,
    # which makes it easy to find matching clusters.
    # ----------------------------------------------------------------------
    records.sort(key=lambda r: r["simkey"])

    # ----------------------------------------------------------------------
    # Walk through the sorted list and collect clusters of records that
    # share the same SimKey. Only print clusters that contain two or more
    # records. Each line is printed as "Input,SimKey" to resemble a
    # two-column CSV file. Clusters are separated by a blank line.
    # ----------------------------------------------------------------------

    current_key = None
    cluster = []

    def print_cluster(c):
        # Only print clusters with two or more records
        if len(c) < 2:
            return
        for rec in c:
            print(f"{rec['input']},{rec['simkey']}")
        print()  # blank line between clusters

    for rec in records:
        if rec["simkey"] != current_key:
            # New SimKey encountered: flush previous cluster
            if cluster:
                print_cluster(cluster)
            current_key = rec["simkey"]
            cluster = [rec]
        else:
            # Same SimKey, add to current cluster
            cluster.append(rec)

    # Flush the last cluster at the end
    if cluster:
        print_cluster(cluster)


if __name__ == "__main__":
    main()
