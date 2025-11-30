// example.ts

// This example shows how to call Interzoid's Business Information API
// using TypeScript with no external libraries, in the simplest way possible.

// NOTE: This example assumes a runtime environment where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define the expected JSON response shape
interface ApiResponse {
  CompanyName: string;
  CompanyURL: string;
  CompanyLocation: string;
  CompanyDescription: string;
  Revenue: string;
  NumberEmployees: string;
  NAICS: string;
  TopExecutive: string;
  TopExecutiveTitle: string;
  Code: string;
  Credits: string;
}

async function main(): Promise<void> {
  // We URL-encode the lookup value for safety, so different inputs will work.
  const lookup = encodeURIComponent("Cisco");

  // Construct the API request URL
  const apiURL =
    "https://api.interzoid.com/getbusinessinfo?license=" +
    encodeURIComponent(API_KEY) +
    "&lookup=" +
    lookup;

  try {
    // Perform the HTTP GET request
    const response = await fetch(apiURL);

    // Check the HTTP status for errors
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse the response into our ApiResponse TypeScript interface
    const data: ApiResponse = await response.json();

    // Print the important fields
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
    // Handle any network or parsing issues
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
