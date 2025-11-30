// example.ts

// This example shows how to call Interzoid's Company Match Advanced API
// using TypeScript with no external libraries, in the simplest way possible.

// NOTE: This example assumes a runtime environment where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define a TypeScript interface that matches the JSON response from the API
interface ApiResponse {
  SimKey: string;
  Code: string;
  Credits: string;
}

// We wrap our logic in an async function so we can use `await`
async function main(): Promise<void> {
  // The endpoint and parameters. In this simple example we hard-code them.
  // You can change the company name or algorithm type as needed.
  const url =
    "https://api.interzoid.com/getcompanymatchadvanced?license=" +
    encodeURIComponent(API_KEY) +
    "&company=" +
    encodeURIComponent("ibm") +
    "&algorithm=" +
    encodeURIComponent("model-v4-wide");

  try {
    // Perform the HTTP GET request
    const response = await fetch(url);

    // Check that the HTTP call itself succeeded (status code 200-299)
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse the response body as JSON.
    // We tell TypeScript to treat it as our ApiResponse shape.
    const data: ApiResponse = await response.json();

    // Print the results
    console.log("Match Similarity Key:", data.SimKey);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);
  } catch (error) {
    // This will catch network errors or JSON parsing errors.
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
