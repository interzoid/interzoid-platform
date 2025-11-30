// Simple Node.js example that:
// - Reads full names from a text file (one per line)
// - Calls Interzoid's Full Name Match API for each name
// - Writes the original value and returned SimKey to a CSV file
//
// Requirements:
// - Node.js 18+ (for built-in fetch)
// - No third-party libraries

const fs = require("fs");
const readline = require("readline");

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Hardcoded input and output file names
const inputFileName = "sample-input-file.txt";
const outputFileName = "output.csv";

// Helper function to escape a value for CSV:
// - Wraps in double quotes if needed
// - Escapes existing double quotes by doubling them
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

  // Use readline to read the input file line by line
  const rl = readline.createInterface({
    input: inputStream,
    crlfDelay: Infinity,
  });

  let lineNumber = 0;

  for await (const line of rl) {
    lineNumber++;
    const originalValue = line;

    // Skip blank lines (optional)
    if (originalValue.length === 0) {
      continue;
    }

    // Build the API URL with proper URL-encoding.
    // URLSearchParams will take care of encoding spaces and special characters.
    const endpoint = "https://api.interzoid.com/getfullnamematch";

    const params = new URLSearchParams({
      license: API_KEY,
      fullname: originalValue,
    });

    const apiURL = `${endpoint}?${params.toString()}`;

    try:
      // Make the HTTP GET request
      const response = await fetch(apiURL);

      // If the response is not OK (e.g. 4xx or 5xx), log an error and write an empty SimKey
      if (!response.ok) {
        console.error(
          `API error on line ${lineNumber} (${JSON.stringify(
            originalValue
          )}): HTTP ${response.status} ${response.statusText}`
        );
        outputStream.write(
          `${csvEscape(originalValue)},${csvEscape("")}\n`
        );
        continue;
      }

      // Read and parse the JSON response
      const data = await response.json();

      // Write one CSV row per input line: original value and SimKey
      const simKey = data.SimKey || "";
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape(simKey)}\n`
      );
    } catch (err) {
      // If there is a network or parsing error, log it and still write a row
      console.error(
        `Error for line ${lineNumber} (${JSON.stringify(
          originalValue
        )}):`,
        err.message
      );
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape("")}\n`
      );
    }
  }

  // Close the output file when done
  outputStream.end(() => {
    console.log("Done. Results written to", outputFileName);
  });
}

// Run the script
main().catch((err) => {
  console.error("Unexpected error:", err.message);
});
