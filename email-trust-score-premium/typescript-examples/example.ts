// example.ts

// This example shows how to call Interzoid's Email Trust Score API
// using TypeScript with no external libraries, in the simplest way possible.

// NOTE: This example assumes a runtime environment where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace with your API key from: https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define the expected shape of the API's JSON response
interface ApiResponse {
  Email: string;
  Score: string;
  Reasoning: string;
  Code: string;
  Credits: string;
}

async function main(): Promise<void> {
  // Email addresses may contain special characters,
  // so we URL-encode them for safety using encodeURIComponent
  const email = encodeURIComponent("billsmith11@gmail.com");

  // Construct the API endpoint
  const apiURL =
    "https://api.interzoid.com/emailtrustscore?license=" +
    encodeURIComponent(API_KEY) +
    "&lookup=" +
    email;

  try {
    // Perform the GET request to the API
    const response = await fetch(apiURL);

    // Check if the HTTP request returned a success response
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse the response body as JSON into our ApiResponse shape
    const data: ApiResponse = await response.json();

    // Print out values from the API response
    console.log("Email:", data.Email);
    console.log("Trust Score:", data.Score);
    console.log("Reasoning:", data.Reasoning);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);
  } catch (error) {
    // Handle possible network or parsing errors
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
