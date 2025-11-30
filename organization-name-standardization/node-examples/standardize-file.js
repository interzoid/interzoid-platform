// Simple Node.js example that:
// - Reads organization names from a text file (one per line)
// - Calls Interzoid's Organization Standardization API
// - Writes original value + standardized value to a CSV file
//
// Requirements:
// - Node.js 18+ (for built-in fetch)
// - No third-party libraries

const fs = require("fs");
const readline = require("readline");

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Input and output file names
const inputFileName = "sample-input-file.txt";
const outputFileName = "output.csv";

// Helper to safely escape values for CSV:
// - Wraps in quotes if needed
// - Doubles any existing quotes
function csvEscape(value) {
  const str = String(value);
  if (/[",\r\n]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

async function main() {
  // Open the input file as a readable stream
  let inputStream;
  try {
    inputStream = fs.createReadStream(inputFileName, { encoding: "utf8" });
  } catch (err) {
    console.error("Error opening input file:", err.message);
    return;
  }

  // Create the output CSV file as a writable stream
  let outputStream;
  try {
    outputStream = fs.createWriteStream(outputFileName, { encoding: "utf8" });
  } catch (err) {
    console.error("Error creating output file:", err.message);
    return;
  }

  // Use readline to process the input file line by line
  const rl = readline.createInterface({
    input: inputStream,
    crlfDelay: Infinity,
  });

  let lineNumber = 0;

  for await (const line of rl) {
    lineNumber++;
    const originalValue = line;

    // Skip blank lines if desired
    if (originalValue.length === 0) {
      continue;
    }

    // Build the API URL. URLSearchParams will safely encode the org name.
    const endpoint = "https://api.interzoid.com/getorgstandard";
    const params = new URLSearchParams({
      license: API_KEY,
      org: originalValue,
    });

    const apiURL = `${endpoint}?${params.toString()}`;

    try {
      // Call the API
      const response = await fetch(apiURL);

      if (!response.ok) {
        console.error(
          `API call error for line ${lineNumber} (${JSON.stringify(
            originalValue
          )}): HTTP ${response.status} ${response.statusText}`
        );
        // Still write a row with an empty standardized value
        outputStream.write(
          `${csvEscape(originalValue)},${csvEscape("")}\n`
        );
        continue;
      }

      // Parse the JSON response
      const data = await response.json();

      // The response contains a "Standard" field with the standardized organization name
      const standard = data.Standard || "";

      // Write row to CSV: originalValue, standardizedValue
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape(standard)}\n`
      );
    } catch (err) {
      console.error(
        `Error processing line ${lineNumber} (${JSON.stringify(
          originalValue
        )}):`,
        err.message
      );
      // On error, still write a row with empty standardized value
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape("")}\n`
      );
    }
  }

  // Close the output file when finished
  outputStream.end(() => {
    console.log("Done. Results written to", outputFileName);
  });
}

// Run the script
main().catch((err) => {
  console.error("Unexpected error:", err.message);
});
