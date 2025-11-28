// Simple Node.js example calling Interzoid's Custom Data Enrichment API
// Uses only the built-in Fetch API (Node.js 18+). No external packages required.

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. The Custom API requires several parameters, including a JSON array for "output".
//    URLSearchParams will encode everything correctly, including spaces and quotes.
const endpoint = "https://api.interzoid.com/getcustom";

const params = new URLSearchParams({
  license: API_KEY,
  topic: "detailed information about companies",
  lookup: "IBM",
  model: "default",
  output: `["headquarters","ceo","website","number of employees","ticker symbol","2023 revenue","2022 revenue"]`,
});

// Build the encoded final URL
const url = `${endpoint}?${params.toString()}`;

// 3. Main function to call the API and print specific fields from the response
async function main() {
  try {
    // Perform the GET request
    const response = await fetch(url);

    // Check for HTTP-level failure
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status, response.statusText);
      return;
    }

    // Parse JSON returned from the API
    const data = await response.json();

    // The API response includes specific fields we requested in the "output" parameter
    console.log("Headquarters:", data["headquarters"]);
    console.log("CEO:", data["ceo"]);
    console.log("Website:", data["website"]);
    console.log("Employees:", data["number of employees"]);
    console.log("Ticker Symbol:", data["ticker symbol"]);
    console.log("2023 Revenue:", data["2023 revenue"]);
    console.log("2022 Revenue:", data["2022 revenue"]);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);

  } catch (error) {
    // Handle network or parsing issues
    console.error("Error calling or processing API:", error.message);
  }
}

// 4. Run the example
main();
