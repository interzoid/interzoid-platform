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
	// Open the input file
	inFile, err := os.Open(inputFileName)
	if err != nil {
		fmt.Println("Error opening input file:", err)
		return
	}
	defer inFile.Close()

	// Create the output CSV file
	outFile, err := os.Create(outputFileName)
	if err != nil {
		fmt.Println("Error creating output file:", err)
		return
	}
	defer outFile.Close()

	csvWriter := csv.NewWriter(outFile)
	defer csvWriter.Flush()

	// JSON mapping struct
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

		// Skip blank lines optionally
		if len(originalValue) == 0 {
			continue
		}

		// URL encode the name
		nameParam := url.QueryEscape(originalValue)

		// Build API URL
		apiURL := "https://api.interzoid.com/getfullnamematch?license=" + API_KEY +
			"&fullname=" + nameParam

		// Make request
		resp, err := http.Get(apiURL)
		if err != nil {
			fmt.Printf("API error on line %d (%q): %v\n", lineNumber, originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		body, err := ioutil.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			fmt.Printf("Response read error for %q: %v\n", originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		var result Response
		if err := json.Unmarshal(body, &result); err != nil {
			fmt.Printf("JSON parse error for %q: %v\n", originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		// Write one row per input line
		err = csvWriter.Write([]string{originalValue, result.SimKey})
		if err != nil {
			fmt.Printf("CSV write error for %q: %v\n", originalValue, err)
		}
	}

	if err := scanner.Err(); err != nil {
		fmt.Println("Error reading input file:", err)
		return
	}

	fmt.Println("Done. Results written to", outputFileName)
}
