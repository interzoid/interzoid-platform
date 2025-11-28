// Simple Node.js example calling Interzoid's Address Match Advanced API
// Uses only built-in Node features (no extra libraries needed).
// Works in Node.js 18+ where the Fetch API is included.

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Base API URL and example parameters.
//    We URL-encode the address because it contains spaces.
const endpoint = "https://api.interzoid.com/getaddressmatchadvanced";

const params = new URLSearchParams({
  license: API_KEY,
  address: "400 East Broadway St",
  algorithm: "model-v3-narrow",
});

// This gives us the full URL including the encoded parameters.
const url = `${endpoint}?${params.toString()}`;

// 3. Main function to call the API and print the results
async function main() {
  try {
    // Call the API with a simple GET
    const response = await fetch(url);

    // If the API returns a non-200 status code, show an error
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status, response.statusText);
      return;
    }

    // 4. Parse the JSON response body
    const data = await response.json();

    // Example fields returned by the API:
    // {
    //   "SimKey": "2627ajs6hskfgauquw^qdhq",
    //   "Code": "Success",
    //   "Credits": "400954"
    // }

    console.log("Address Similarity Key:", data.SimKey);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);

  } catch (error) {
    // Handle network or JSON parsing errors
    console.error("Error calling or processing API:", error.message);
  }
}

// 5. Run the example
main();
