// Simple Node.js example calling Interzoid's Full Name Match API
// Uses only built-in features: no external libraries required.
// Works in Node.js 18+ (Fetch API is built in).

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Base endpoint for the Full Name Match API.
//    You can change the full name or algorithm parameter as needed.
const endpoint = "https://api.interzoid.com/getfullnamematch";

// 3. Construct the API URL. We include the name and the license key as query parameters.
const params = new URLSearchParams({
  license: API_KEY,
  fullname: "James Johnston", // name to match
});

const url = `${endpoint}?${params.toString()}`;

// 4. Main function to call the API and handle any errors
async function main() {
  try {
    // Perform the HTTPS GET request
    const response = await fetch(url);

    // Check for an HTTP-level error
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status, response.statusText);
      return;
    }

    // 5. Parse the JSON response
    const data = await response.json();

    // The API returns fields like:
    // {
    //   "SimKey": "xyz123",
    //   "Code": "Success",
    //   "Credits": "20034"
    // }

    console.log("Similarity Key:", data.SimKey);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);

  } catch (error) {
    // Errors here are usually networking or JSON parsing issues
    console.error("Error calling or processing API:", error.message);
  }
}

// 6. Run the example
main();
