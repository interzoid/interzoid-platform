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

	// Lookup value may require URL encoding
	lookup := url.QueryEscape("informatica")

	apiURL := "https://api.interzoid.com/getparentcompanyinfo?license=" + API_KEY +
		"&lookup=" + lookup

	// Perform the GET request
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

	// Struct reflects JSON returned by the API
	type Response struct {
		CompanyName                 string `json:"CompanyName"`
		CompanyURL                  string `json:"CompanyURL"`
		ParentCompany               string `json:"ParentCompany"`
		ParentCompanyLocation       string `json:"ParentCompanyLocation"`
		ParentCompanyURL            string `json:"ParentCompanyURL"`
		ParentCompanyDescription    string `json:"ParentCompanyDescription"`
		ParentCompanyReferenceURL   string `json:"ParentCompanyReferenceURL"`
		Code                        string `json:"Code"`
		Credits                     string `json:"Credits"`
	}

	var result Response

	// Parse the JSON into our Go struct
	err = json.Unmarshal(body, &result)
	if err != nil {
		fmt.Println("Error parsing JSON:", err)
		return
	}

	// Print selected fields from the response
	fmt.Println("Company Name:", result.CompanyName)
	fmt.Println("Company URL:", result.CompanyURL)
	fmt.Println("Parent Company:", result.ParentCompany)
	fmt.Println("Parent Location:", result.ParentCompanyLocation)
	fmt.Println("Parent Website:", result.ParentCompanyURL)
	fmt.Println("Parent Description:", result.ParentCompanyDescription)
	fmt.Println("Parent Reference URL:", result.ParentCompanyReferenceURL)
	fmt.Println("Result Code:", result.Code)
	fmt.Println("Remaining Credits:", result.Credits)
}
