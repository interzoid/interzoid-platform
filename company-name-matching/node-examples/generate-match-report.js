// generate-match-report.js

const fs = require("fs");
const https = require("https");

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Input file containing one company name per line
const INPUT_FILE_NAME = "sample-input-file.txt";

/**
 * Calls Interzoid's getcompanymatchadvanced API for a single company name
 * and returns a Promise resolving to the similarity key (SimKey) as a string.
 * Returns an empty string on error or if SimKey is missing.
 */
function callCompanyMatchAPI(companyName) {
  return new Promise((resolve) => {
    // URL-encode the company name to safely embed it in the query string
    const companyParam = encodeURIComponent(companyName);

    const apiURL =
      "https://api.interzoid.com/getcompanymatchadvanced" +
      `?license=${API_KEY}` +
      `&company=${companyParam}` +
      "&algorithm=model-v4-wide";

    https
      .get(apiURL, (res) => {
        let data = "";

        res.on("data", (chunk) => {
          data += chunk;
        });

        res.on("end", () => {
          try {
            const json = JSON.parse(data);
            const simKey = json.SimKey || "";
            resolve(simKey);
          } catch (err) {
            console.error(
              `Error parsing JSON for "${companyName}": ${err.message}`
            );
            console.error("Raw response:", data);
            resolve("");
          }
        });
      })
      .on("error", (err) => {
        console.error(`Error calling API for "${companyName}": ${err.message}`);
        resolve("");
      });
  });
}

async function main() {
  // Each record will hold the original input and its similarity key
  const records = [];

  // Read the input file contents
  let fileContents;
  try {
    fileContents = fs.readFileSync(INPUT_FILE_NAME, "utf8");
  } catch (err) {
    console.error("Error reading input file:", err.message);
    return;
  }

  // Split into lines and process each non-empty line
  const lines = fileContents.split(/\r?\n/);

  for (const line of lines) {
    const company = line.trim();

    // Skip blank lines
    if (!company) continue;

    const simKey = await callCompanyMatchAPI(company);

    // Skip if no SimKey returned
    if (!simKey) continue;

    records.push({ input: company, simKey });
  }

  if (records.length === 0) {
    console.log("No records with similarity keys found.");
    return;
  }

  //--------------------------------------------------------------------
  // Sort records strictly by simKey only so that all matching keys
  // are adjacent in the array. This makes it easy to find clusters.
  //--------------------------------------------------------------------
  records.sort((a, b) => a.simKey.localeCompare(b.simKey));

  //--------------------------------------------------------------------
  // Walk the sorted list and build clusters of records that share
  // the same simKey. Only print clusters of size >= 2.
  // Each printed line is "Input,SimKey" (two-column CSV style),
  // and clusters are separated by a blank line.
  //--------------------------------------------------------------------
  let currentKey = null;
  let cluster = [];

  function printCluster(c) {
    if (c.length < 2) return; // Only print clusters with 2 or more
    for (const r of c) {
      console.log(`${r.input},${r.simKey}`);
    }
    console.log(); // blank line between clusters
  }

  for (const rec of records) {
    if (rec.simKey !== currentKey) {
      // New simKey: flush previous cluster
      if (cluster.length > 0) {
        printCluster(cluster);
      }
      currentKey = rec.simKey;
      cluster = [rec];
    } else {
      // Same simKey: add to current cluster
      cluster.push(rec);
    }
  }

  // Flush the final cluster
  if (cluster.length > 0) {
    printCluster(cluster);
  }
}

// Run the main function
main().catch((err) => {
  console.error("Unexpected error:", err);
});
