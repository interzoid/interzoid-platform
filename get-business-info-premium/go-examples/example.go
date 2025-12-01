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

	// We URL-encode the lookup value so it works for all input variations.
	lookup := url.QueryEscape("Cisco")

	apiURL := "https://api.interzoid.com/getbusinessinfo?license=" + API_KEY +
		"&lookup=" + lookup

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

	// Struct maps to the fields returned by the API.
	type Response struct {
		CompanyName        string `json:"CompanyName"`
		CompanyURL         string `json:"CompanyURL"`
		CompanyLocation    string `json:"CompanyLocation"`
		CompanyDescription string `json:"CompanyDescription"`
		Revenue            string `json:"Revenue"`
		NumberEmployees    string `json:"NumberEmployees"`
		NAICS              string `json:"NAICS"`
		TopExecutive       string `json:"TopExecutive"`
		TopExecutiveTitle  string `json:"TopExecutiveTitle"`
		Code               string `json:"Code"`
		Credits            string `json:"Credits"`
	}

	var result Response

	// Parse the JSON into our struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print the key parts of the response
	fmt.Println("Company Name:", result.CompanyName)
	fmt.Println("Website:", result.CompanyURL)
	fmt.Println("Location:", result.CompanyLocation)
	fmt.Println("Description:", result.CompanyDescription)
	fmt.Println("Revenue:", result.Revenue)
	fmt.Println("Employees:", result.NumberEmployees)
	fmt.Println("NAICS:", result.NAICS)
	fmt.Println("Top Executive:", result.TopExecutive)
	fmt.Println("Top Executive Title:", result.TopExecutiveTitle)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
