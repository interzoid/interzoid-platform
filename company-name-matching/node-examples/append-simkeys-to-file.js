// Simple Node.js example that:
// - Reads company names from a text file (one per line)
// - Calls Interzoid's Company Match Advanced API for each company
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
  // Open input file as a stream
  let inputStream;
  try {
    inputStream = fs.createReadStream(inputFileName, { encoding: "utf8" });
  } catch (err) {
    console.error("Error opening input file:", err.message);
    return;
  }

  // Create output stream for CSV
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
    crlfDelay: Infinity, // Handle all common line endings
  });

  let lineNumber = 0;

  for await (const line of rl) {
    lineNumber++;
    const originalValue = line;

    // Skip completely empty lines (optional; remove this if you want to process them)
    if (originalValue.length === 0) {
      continue;
    }

    // Build the API URL with proper URL-encoding
    // URLSearchParams will encode the company value safely
    const endpoint = "https://api.interzoid.com/getcompanymatchadvanced";
    const params = new URLSearchParams({
      license: API_KEY,
      company: originalValue,
      algorithm: "model-v4-wide",
    });

    const apiURL = `${endpoint}?${params.toString()}`;

    try {
      // Call the API
      const response = await fetch(apiURL);

      if (!response.ok) {
        console.error(
          `Error calling API for line ${lineNumber} (${JSON.stringify(
            originalValue
          )}): HTTP ${response.status} ${response.statusText}`
        );
        // Write row with empty SimKey so CSV still lines up
        outputStream.write(
          `${csvEscape(originalValue)},${csvEscape("")}\n`
        );
        continue;
      }

      // Read and parse JSON response
      const data = await response.json();

      // Optional: check result code for success
      if (data.Code !== "Success") {
        console.warn(
          `Non-success code for line ${lineNumber} (${JSON.stringify(
            originalValue
          )}): Code=${data.Code}`
        );
      }

      // Write original value and SimKey as a CSV row
      // csvEscape ensures commas/quotes/newlines are handled correctly
      const simKey = data.SimKey || "";
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape(simKey)}\n`
      );
    } catch (err) {
      console.error(
        `Error calling or processing API for line ${lineNumber} (${JSON.stringify(
          originalValue
        )}):`,
        err.message
      );
      // On error, still write a row with an empty SimKey
      outputStream.write(
        `${csvEscape(originalValue)},${csvEscape("")}\n`
      );
    }
  }

  // Close the output file
  outputStream.end(() => {
    console.log("Done. Results written to", outputFileName);
  });
}

// Run the script
main().catch((err) => {
  console.error("Unexpected error:", err.message);
});
