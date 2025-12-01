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

	// Base API endpoint with example parameters.
	// Note: we URL-encode the address since it contains spaces.
	address := url.QueryEscape("400 East Broadway St")

	apiURL := "https://api.interzoid.com/getaddressmatchadvanced?license=" + API_KEY +
		"&address=" + address +
		"&algorithm=model-v3-narrow"

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

	// Struct to map the JSON response
	type Response struct {
		SimKey  string `json:"SimKey"`
		Code    string `json:"Code"`
		Credits string `json:"Credits"`
	}

	var result Response

	// Convert the JSON into our struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print the results
	fmt.Println("Address Similarity Key:", result.SimKey)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
