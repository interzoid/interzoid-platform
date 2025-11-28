// Simple Node.js example calling Interzoid's Organization Standardization API
// No external libraries required. Uses built-in Fetch API (Node.js 18+).

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Example base endpoint. The organization name may contain punctuation or spaces,
//    so we pass it through URLSearchParams to ensure it is encoded safely.
const endpoint = "https://api.interzoid.com/getorgstandard";

const params = new URLSearchParams({
  license: API_KEY,
  org: "b.o.a.", // sample organization name
});

// Build the full URL with encoded parameters
const url = `${endpoint}?${params.toString()}`;

// 3. Main function to call the API and print the results
async function main() {
  try {
    // Call the API using a GET request
    const response = await fetch(url);

    // Check for HTTP status errors
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status, response.statusText);
      return;
    }

    // Parse the JSON response body
    const data = await response.json();

    // Expected fields:
    // {
    //   "Standard": "Bank of America",
    //   "Code": "Success",
    //   "Credits": "201234"
    // }

    console.log("Standardized Organization:", data.Standard);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);

  } catch (error) {
    // Handle networking or JSON parsing errors
    console.error("Error calling or processing API:", error.message);
  }
}

// 4. Run the example
main();
