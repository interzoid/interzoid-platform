// generate-match-report.js
//
// Reads full names from sample-input-file.txt, calls Interzoid's
// getfullnamematch API to generate similarity keys (SimKeys), then
// prints clusters of names that share the same SimKey.
//
// Each printed line is "Input,SimKey" (two-column CSV style).
// Only clusters with two or more records are printed.

const fs = require("fs");
const https = require("https");

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Input file containing one full name per line
const INPUT_FILE_NAME = "sample-input-file.txt";

/**
 * Calls Interzoid's getfullnamematch API for a single full name
 * and returns a Promise that resolves to the similarity key (SimKey).
 * Returns an empty string on error or if SimKey is missing.
 */
function callFullNameMatchAPI(fullName) {
  return new Promise((resolve) => {
    const encodedName = encodeURIComponent(fullName);

    const apiURL =
      "https://api.interzoid.com/getfullnamematch" +
      `?license=${API_KEY}` +
      `&fullname=${encodedName}`;

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
              `Error parsing JSON for "${fullName}": ${err.message}`
            );
            console.error("Raw response:", data);
            resolve("");
          }
        });
      })
      .on("error", (err) => {
        console.error(`Error calling API for "${fullName}": ${err.message}`);
        resolve("");
      });
  });
}

async function main() {
  const records = [];

  // Read the input file
  let fileContents;
  try {
    fileContents = fs.readFileSync(INPUT_FILE_NAME, "utf8");
  } catch (err) {
    console.error("Error reading input file:", err.message);
    return;
  }

  const lines = fileContents.split(/\r?\n/);

  // Process each non-empty line as a full name
  for (const line of lines) {
    const fullName = line.trim();
    if (!fullName) continue;

    const simKey = await callFullNameMatchAPI(fullName);

    if (!simKey) continue;

    records.push({ input: fullName, simKey });
  }

  if (records.length === 0) {
    console.log("No records with similarity keys found.");
    return;
  }

  //--------------------------------------------------------------------
  // Sort by simKey only so that matching keys are adjacent
  //--------------------------------------------------------------------
  records.sort((a, b) => a.simKey.localeCompare(b.simKey));

  //--------------------------------------------------------------------
  // Build and print clusters: only clusters of size >= 2.
  // Each line is "Input,SimKey", clusters separated by blank line.
  //--------------------------------------------------------------------
  let currentKey = null;
  let cluster = [];

  function printCluster(c) {
    if (c.length < 2) return;
    for (const r of c) {
      console.log(`${r.input},${r.simKey}`);
    }
    console.log(); // blank line
  }

  for (const rec of records) {
    if (rec.simKey !== currentKey) {
      if (cluster.length > 0) {
        printCluster(cluster);
      }
      currentKey = rec.simKey;
      cluster = [rec];
    } else {
      cluster.push(rec);
    }
  }

  // Flush last cluster
  if (cluster.length > 0) {
    printCluster(cluster);
  }
}

main().catch((err) => {
  console.error("Unexpected error:", err);
});
