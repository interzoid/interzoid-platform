// Simple Node.js example that:
// - Reads street addresses from a file (one per line)
// - Calls Interzoid's Address Match Advanced API
// - Writes the original address + SimKey to a CSV output
//
// Requirements:
// - Node.js 18+ (built-in fetch)
// - No external libraries needed

const fs = require("fs");
const readline = require("readline");

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Input and output file names
const inputFileName = "sample-input-file.txt";
const outputFileName = "output.csv";

// Helper to escape values properly for CSV output
function csvEscape(value) {
  const str = String(value);
  if (/[",\r\n]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

async function main() {
  // Open the input file
  let inputStream;
  try {
    inputStream = fs.createReadStream(inputFileName, { encoding: "utf8" });
  } catch (err) {
    console.error("Error opening input file:", err.message);
    return;
  }

  // Create output file
  let outputStream;
  try {
    outputStream = fs.createWriteStream(outputFileName, { encoding: "utf8" });
  } catch (err) {
    console.error("Error creating output file:", err.message);
    return;
  }

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

    // Construct the URL with encoded parameters
    const endpoint = "https://api.interzoid.com/getaddressmatchadvanced";
    const params = new URLSearchParams({
      license: API_KEY,
      address: originalValue,
      algorithm: "model-v3-narrow",
    });

    const apiURL = `${endpoint}?${params.toString()}`;

    try {
      // Call the API
      const response = await fetch(apiURL);

      if (!response.ok) {
        console.error(
          `Error calling API for line ${lineNumber} (${JSON.stringify(
            originalValue
          )}): HTTP ${response.status}`
        );
        outputStream.write(
          `${csvEscape(originalValue)},${csvEscape("")}\n`
        );
        continue;
      }

      // Parse JSON
      const data = await response.json();

      // Write a valid CSV row: address + SimKey
      const simKey = data.SimKey || "";
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape(simKey)}\n`
      );

    } catch (err) {
      console.error(
        `Error calling API for ${JSON.stringify(originalValue)}:`,
        err.message
      );
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape("")}\n`
      );
    }
  }

  outputStream.end(() => {
    console.log("Done. Results written to", outputFileName);
  });
}

// Run the script
main().catch((err) => {
  console.error("Unexpected error:", err.message);
});
