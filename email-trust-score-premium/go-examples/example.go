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

	// Email address may contain characters that need encoding,
	// so we use url.QueryEscape for safety.
	email := url.QueryEscape("billsmith11@gmail.com")

	apiURL := "https://api.interzoid.com/emailtrustscore?license=" + API_KEY +
		"&lookup=" + email

	// Perform the HTTP GET request
	resp, err := http.Get(apiURL)
	if err != nil {
		fmt.Println("Error calling API:", err)
		return
	}
	defer resp.Body.Close()

	// Read the response
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading API response:", err)
		return
	}

	// Struct matches the JSON that is returned by the API.
	type Response struct {
		Email     string `json:"Email"`
		Score     string `json:"Score"`
		Reasoning string `json:"Reasoning"`
		Code      string `json:"Code"`
		Credits   string `json:"Credits"`
	}

	var result Response

	// Convert JSON returned by API into our Go struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print values from the API response
	fmt.Println("Email:", result.Email)
	fmt.Println("Trust Score:", result.Score)
	fmt.Println("Reasoning:", result.Reasoning)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
