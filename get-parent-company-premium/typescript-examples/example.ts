// example.ts

// This example shows how to call Interzoid's Parent Company Information API
// using TypeScript with no external libraries, in the simplest way possible.

// NOTE: This example assumes a runtime where `fetch` is available,
// such as Node.js 18+ or a modern browser.

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// Define TypeScript interface that matches the JSON response from the API
interface ApiResponse {
  CompanyName: string;
  CompanyURL: string;
  ParentCompany: string;
  ParentCompanyLocation: string;
  ParentCompanyURL: string;
  ParentCompanyDescription: string;
  ParentCompanyReferenceURL: string;
  Code: string;
  Credits: string;
}

async function main(): Promise<void> {
  // URL-encode the lookup value for safety
  const lookup = encodeURIComponent("informatica");

  // Build the API URL
  const apiURL =
    "https://api.interzoid.com/getparentcompanyinfo?license=" +
    encodeURIComponent(API_KEY) +
    "&lookup=" +
    lookup;

  try {
    // Perform the HTTP GET request
    const response = await fetch(apiURL);

    // Check for HTTP status errors
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status);
      return;
    }

    // Parse the response JSON into our ApiResponse interface
    const data: ApiResponse = await response.json();

    // Print fields from the API response
    console.log("Company Name:", data.CompanyName);
    console.log("Company URL:", data.CompanyURL);
    console.log("Parent Company:", data.ParentCompany);
    console.log("Parent Location:", data.ParentCompanyLocation);
    console.log("Parent Website:", data.ParentCompanyURL);
    console.log("Parent Description:", data.ParentCompanyDescription);
    console.log("Parent Reference URL:", data.ParentCompanyReferenceURL);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);
  } catch (error) {
    // Catch networking or JSON parsing issues
    console.error("Error calling or parsing API:", error);
  }
}

// Run the example
main();
