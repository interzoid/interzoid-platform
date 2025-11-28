// Simple Node.js example calling Interzoid's Email Trust Score API
// Uses only the built-in Fetch API (Node.js 18+). No external libraries required.

// 1. Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE";

// 2. Base API endpoint. We use URLSearchParams to safely encode the email address.
const endpoint = "https://api.interzoid.com/emailtrustscore";

const params = new URLSearchParams({
  license: API_KEY,
  lookup: "billsmith11@gmail.com", // encoded automatically
});

const url = `${endpoint}?${params.toString()}`;

// 3. Main function to call the API and display the response
async function main() {
  try {
    // Perform the HTTP GET request
    const response = await fetch(url);

    // Check for HTTP-level errors
    if (!response.ok) {
      console.error("Error calling API. HTTP status:", response.status, response.statusText);
      return;
    }

    // Parse the JSON returned from the API
    const data = await response.json();

    // Example structure returned:
    // {
    //   "Email": "...",
    //   "Score": "...",
    //   "Reasoning": "...",
    //   "Code": "...",
    //   "Credits": "..."
    // }

    console.log("Email:", data.Email);
    console.log("Trust Score:", data.Score);
    console.log("Reasoning:", data.Reasoning);
    console.log("Result Code:", data.Code);
    console.log("Remaining Credits:", data.Credits);

  } catch (error) {
    // Handle network or JSON parsing problems
    console.error("Error calling or processing API:", error.message);
  }
}

// 4. Run the example
main();
