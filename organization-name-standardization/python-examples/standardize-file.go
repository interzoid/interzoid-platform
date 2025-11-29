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

// Replace with your key: https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE"

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

	// Create output CSV file
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
		Standard string `json:"Standard"`
		Code     string `json:"Code"`
		Credits  string `json:"Credits"`
	}

	scanner := bufio.NewScanner(inFile)
	lineNumber := 0

	for scanner.Scan() {
		lineNumber++
		originalValue := scanner.Text()

		// Allow blank lines to be skipped
		if len(originalValue) == 0 {
			continue
		}

		// Encode organization name
		orgParam := url.QueryEscape(originalValue)

		// Build the request URL
		apiURL := "https://api.interzoid.com/getorgstandard?license=" + API_KEY +
			"&org=" + orgParam

		// Issue request
		resp, err := http.Get(apiURL)
		if err != nil {
			fmt.Printf("API call error for line %d (%q): %v\n", lineNumber, originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		body, err := ioutil.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			fmt.Printf("Error reading response for %q: %v\n", originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		var result Response
		if err := json.Unmarshal(body, &result); err != nil {
			fmt.Printf("JSON parse error for %q: %v\n", originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		// Write row to CSV
		err = csvWriter.Write([]string{originalValue, result.Standard})
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