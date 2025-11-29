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

	// Open input file
	inFile, err := os.Open(inputFileName)
	if err != nil {
		fmt.Println("Error opening input file:", err)
		return
	}
	defer inFile.Close()

	// Create CSV output file
	outFile, err := os.Create(outputFileName)
	if err != nil {
		fmt.Println("Error creating output file:", err)
		return
	}
	defer outFile.Close()

	csvWriter := csv.NewWriter(outFile)
	defer csvWriter.Flush()

	// API response struct
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

		// skip empty lines if desired
		if len(originalValue) == 0 {
			continue
		}

		addressParam := url.QueryEscape(originalValue)

		// Construct endpoint
		apiURL := "https://api.interzoid.com/getaddressmatchadvanced?license=" + API_KEY +
			"&address=" + addressParam +
			"&algorithm=model-v3-narrow"

		// Perform request
		resp, err := http.Get(apiURL)
		if err != nil {
			fmt.Printf("Error calling API for line %d (%q): %v\n",
				lineNumber, originalValue, err)
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
			fmt.Printf("Error parsing JSON for %q: %v\n", originalValue, err)
			_ = csvWriter.Write([]string{originalValue, ""})
			continue
		}

		// Write a valid CSV row
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
