
package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
)

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE"

func main() {

	// The organization name may contain punctuation or spaces,
	// so we URL-encode it for safety.
	org := url.QueryEscape("b.o.a.")

	apiURL := "https://api.interzoid.com/getorgstandard?license=" + API_KEY +
		"&org=" + org

	// Make the HTTP GET request
	resp, err := http.Get(apiURL)
	if err != nil {
		fmt.Println("Error calling API:", err)
		return
	}
	defer resp.Body.Close()

	// Read the response body
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading API response:", err)
		return
	}

	// Struct to represent JSON response fields
	type Response struct {
		Standard string `json:"Standard"`
		Code     string `json:"Code"`
		Credits  string `json:"Credits"`
	}

	var result Response

	// Convert the JSON returned by the API into our Go struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print the results
	fmt.Println("Standardized Organization:", result.Standard)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
