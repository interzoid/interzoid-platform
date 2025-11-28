package main

import (
	"bufio"
	"encoding/csv"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
)

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE"

// Hardcoded input and output file names
const inputFileName = "sample-input-file.txt"
const outputFileName = "output.csv"

func main() {
	// Open input file
	inFile, err := os.Open(inputFileName)
	if err != nil {
		fmt.Println("Error opening input file:", err)
		return
	}
	defer inFile.Close()

	// Create output CSV file
	outFile, err := os.Create(outputFileName)
	if err != nil {
		fmt.Println("Error creating output file:", err)
		return
	}
	defer outFile.Close()

	csvWriter := csv.NewWriter(outFile)
	defer csvWriter.Flush()

	// Define struct to map JSON response
	type Response struct {
		SimKey  string `json:"SimKey"`
		Code    string `json:"Code"`
		Credits string `json:"Credits"`
	}

	scanner := bufio.NewScanner(inFile)
	lineNumber := 0

	for scanner.Scan() {
		lineNumber++
		originalValue := scanner.Text()

		// Skip completely empty lines (optional; remove this if you want to process them)
		if len(originalValue) == 0 {
			continue
		}

		// URL-encode the company name
		companyParam := url.QueryEscape(originalValue)

		// Build API URL
		apiURL := "https://api.interzoid.com/getcompanymatchadvanced?license=" + API_KEY +
			"&company=" + companyParam +
			"&algorithm=model-v4-wide"

		// Call the API
		resp, err := http.Get(apiURL)
		if err != nil {
			fmt.Printf("Error calling API for line %d (%q): %v\n", lineNumber, originalValue, err)
			// Write row with empty SimKey so CSV still lines up
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		body, err := ioutil.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			fmt.Printf("Error reading API response for line %d (%q): %v\n", lineNumber, originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		var result Response
		if err := json.Unmarshal(body, &result); err != nil {
			fmt.Printf("Error parsing JSON for line %d (%q): %v\n", lineNumber, originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		// Optional: check result.Code for "Success"
		if result.Code != "Success" {
			fmt.Printf("Non-success code for line %d (%q): Code=%s\n", lineNumber, originalValue, result.Code)
		}

		// Write original value and SimKey as a CSV row.
		// encoding/csv will correctly escape any commas or quotes.
		if err := csvWriter.Write([]string{originalValue, result.SimKey}); err != nil {
			fmt.Printf("Error writing CSV row for line %d (%q): %v\n", lineNumber, originalValue, err)
			continue
		}
	}

	if err := scanner.Err(); err != nil {
		fmt.Println("Error reading input file:", err)
		return
	}

	fmt.Println("Done. Results written to", outputFileName)
}
