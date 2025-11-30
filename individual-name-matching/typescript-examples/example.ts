// example.ts

// This example shows how to call Interzoid's Full Name Match API
// using TypeScript with no external libraries, in the simplest way possible.

// NOTE: This example assumes a runtime environment where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define the TypeScript interface that matches the JSON response
interface ApiResponse {
  SimKey: string;
  Code: string;
  Credits: string;
}

async function main(): Promise<void> {
  // API endpoint with example parameters.
  // Change the "fullname" or algorithm as needed.
  const url =
    "https://api.interzoid.com/getfullnamematch?license=" +
    encodeURIComponent(API_KEY) +
    "&fullname=" +
    encodeURIComponent("James Johnston");

  try {
    // Perform the HTTP GET request
    const response = await fetch(url);

    // If the response is not HTTP 200–299, treat it as an error
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse the response JSON into our ApiResponse type
    const data: ApiResponse = await response.json();

    // Display the results
    console.log("Similarity Key:", data.SimKey);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);
  } catch (error) {
    // If network or parsing error occurs, handle it here
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
