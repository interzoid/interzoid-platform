// example.ts

// This example shows how to call Interzoid's Custom Data Enrichment API
// using TypeScript with no external libraries.

// NOTE: This example assumes a runtime where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define the expected shape of the JSON response
interface ApiResponse {
  headquarters: string;
  ceo: string;
  website: string;
  "number of employees": string;
  "ticker symbol": string;
  "2023 revenue": string;
  "2022 revenue": string;
  Code: string;
  Credits: string;
}

async function main(): Promise<void> {
  // URL-encode parameters so special characters are preserved
  const topic = encodeURIComponent("detailed information about companies");
  const lookup = encodeURIComponent("IBM");
  const output = encodeURIComponent(
    `["headquarters", "ceo", "website","number of employees","ticker symbol","2023 revenue","2022 revenue"]`
  );

  // Build the API endpoint
  const apiURL =
    "https://api.interzoid.com/getcustom?license=" +
    encodeURIComponent(API_KEY) +
    "&topic=" +
    topic +
    "&lookup=" +
    lookup +
    "&model=default" +
    "&output=" +
    output;

  try {
    // Make the HTTP request
    const response = await fetch(apiURL);

    // Check the HTTP status code for errors
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse the JSON response into our ApiResponse interface
    const data: ApiResponse = await response.json();

    // Print the results
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
    // Handle network and parsing errors
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
