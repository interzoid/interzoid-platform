
take this golang program and convert it to Node as a code sample for Github. Do not use any third party libraries. Make sure there are explanatory comments. Do not mention Go. Use the simplest approach simple for a beginner to Interzoid. 

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

	// The endpoint and parameters. In this simple example we hard-code them.
	// You can change the company name or algorithm type as needed.
	url := "https://api.interzoid.com/getcompanymatchadvanced?license=" + API_KEY +
		"&company=ibm&algorithm=model-v4-wide"

	// Perform the HTTP request
	resp, err := http.Get(url)
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

	// Define a struct to map the JSON response
	type Response struct {
		SimKey  string `json:"SimKey"`
		Code    string `json:"Code"`
		Credits string `json:"Credits"`
	}

	var result Response

	// Convert the JSON into our Go struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print the results
	fmt.Println("Match Similarity Key:", result.SimKey)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}