// Simple Node.js example calling Interzoid's Business Info API
// Uses only built-in functionality. No external libraries are required.
// Works in Node.js 18+ (Fetch API supported).

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Base API endpoint. We use URLSearchParams to encode the lookup value correctly.
//    This ensures the value will work even if it contains spaces or punctuation.
const endpoint = "https://api.interzoid.com/getbusinessinfo";

const params = new URLSearchParams({
  license: API_KEY,
  lookup: "Cisco",
});

// Build full URL including the encoded parameters
const url = `${endpoint}?${params.toString()}`;

// 3. Main function that calls the API and prints some useful fields
async function main() {
  try {
    // Make the HTTP GET request
    const response = await fetch(url);

    // Check if we received a successful response
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status, response.statusText);
      return;
    }

    // Parse the response body as JSON
    const data = await response.json();

    // The API returns useful company information such as:
    // - name
    // - website
    // - location
    // - description
    // - revenue
    // - number of employees
    // - industry code
    // - top executive and title

    console.log("Company Name:", data.CompanyName);
    console.log("Website:", data.CompanyURL);
    console.log("Location:", data.CompanyLocation);
    console.log("Description:", data.CompanyDescription);
    console.log("Revenue:", data.Revenue);
    console.log("Employees:", data.NumberEmployees);
    console.log("NAICS:", data.NAICS);
    console.log("Top Executive:", data.TopExecutive);
    console.log("Top Executive Title:", data.TopExecutiveTitle);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);

  } catch (error) {
    // Handle network and parsing problems
    console.error("Error calling or processing API:", error.message);
  }
}

// 4. Run the example
main();
