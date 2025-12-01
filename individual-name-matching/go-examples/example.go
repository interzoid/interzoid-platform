package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
)

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE"

func main() {

	// API endpoint with example parameters
	// Change the "fullname" or algorithm as needed.
	url := "https://api.interzoid.com/getfullnamematch?license=" + API_KEY +
		"&fullname=James%20Johnston"

	// Make the HTTP GET request
	resp, err := http.Get(url)
	if err != nil {
		fmt.Println("Error calling API:", err)
		return
	}
	defer resp.Body.Close()

	// Read all response data
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading API response:", err)
		return
	}

	// Struct to map the JSON response.
	// Note Credits is a STRING.
	type Response struct {
		SimKey  string `json:"SimKey"`
		Code    string `json:"Code"`
		Credits string `json:"Credits"`
	}

	var result Response

	// Convert JSON from the API into our struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print results
	fmt.Println("Similarity Key:", result.SimKey)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
