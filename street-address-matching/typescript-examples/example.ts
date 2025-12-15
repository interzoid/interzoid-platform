// example.ts

// This example shows how to call Interzoid's Address Match Advanced API
// using TypeScript with no external libraries, in the simplest way possible.

// NOTE: This example assumes a runtime where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define the structure of the expected API response
interface ApiResponse {
  SimKey: string;
  Code: string;
  Credits: string;
}

async function main(): Promise<void> {
  // URL-encode the address since it contains spaces or other characters
  const address = encodeURIComponent("400 East Broadway St");

  // Construct the API endpoint URL
  const apiURL =
    "https://api.interzoid.com/getaddressmatchadvanced?license=" +
    encodeURIComponent(API_KEY) +
    "&address=" +
    address +
    "&algorithm=model-v3-narrow";

  try {
    // Perform the HTTP GET request
    const response = await fetch(apiURL);

    // Check if the HTTP response indicates success (status 200–299)
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse JSON response into our TypeScript interface
    const data: ApiResponse = await response.json();

    // Print the response fields
    console.log("Address Similarity Key:", data.SimKey);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);
  } catch (error) {
    // Handle network or JSON parsing exceptions
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
