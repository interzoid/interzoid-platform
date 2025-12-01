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

	// Define each parameter. The output value is a JSON array and must be URL-encoded.
	topic := url.QueryEscape("detailed information about companies")
	lookup := url.QueryEscape("IBM")
	output := url.QueryEscape(`["headquarters", "ceo", "website","number of employees","ticker symbol","2023 revenue","2022 revenue"]`)

	apiURL := "https://api.interzoid.com/getcustom?license=" + API_KEY +
		"&topic=" + topic +
		"&lookup=" + lookup +
		"&model=default" +
		"&output=" + output

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

	// Struct for the JSON response. These keys match the sample.
	type Response struct {
		Headquarters       string `json:"headquarters"`
		CEO                string `json:"ceo"`
		Website            string `json:"website"`
		Employees          string `json:"number of employees"`
		TickerSymbol       string `json:"ticker symbol"`
		Revenue2023        string `json:"2023 revenue"`
		Revenue2022        string `json:"2022 revenue"`
		Code               string `json:"Code"`
		Credits            string `json:"Credits"`
	}

	var result Response

	// Parse JSON into struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print the results
	fmt.Println("Headquarters:", result.Headquarters)
	fmt.Println("CEO:", result.CEO)
	fmt.Println("Website:", result.Website)
	fmt.Println("Employees:", result.Employees)
	fmt.Println("Ticker Symbol:", result.TickerSymbol)
	fmt.Println("2023 Revenue:", result.Revenue2023)
	fmt.Println("2022 Revenue:", result.Revenue2022)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
