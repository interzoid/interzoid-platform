// generate-address-match-report.go
//
// This example reads a list of street addresses from a text file,
// generates similarity keys (SimKeys) using Interzoid's Street Address
// Matching API, then groups and prints addresses that share the same
// SimKey.
//
// Each printed line is "Input,SimKey" (two-column CSV style).
// Only clusters with two or more matching addresses are printed.

package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"sort"
)

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE"

// Input file containing one street address per line
const inputFileName = "sample-input-file.txt"

func main() {
	// Record holds one input address and its generated similarity key
	type Record struct {
		Input  string
		SimKey string
	}

	// Response maps the JSON returned by the getaddressmatchadvanced API
	type Response struct {
		SimKey  string `json:"SimKey"`
		Code    string `json:"Code"`
		Credits string `json:"Credits"`
	}

	// Open the input file for reading
	file, err := os.Open(inputFileName)
	if err != nil {
		fmt.Println("Error opening input file:", err)
		return
	}
	defer file.Close()

	var records []Record

	scanner := bufio.NewScanner(file)

	// Read each line as a street address and call the API
	for scanner.Scan() {
		address := scanner.Text()

		// Skip blank lines
		if address == "" {
			continue
		}

		// URL-encode the address so it can be safely passed as a parameter
		addressParam := url.QueryEscape(address)

		// Build the getaddressmatchadvanced API URL.
		// You can change the algorithm parameter as needed.
		apiURL := "https://api.interzoid.com/getaddressmatchadvanced?license=" + API_KEY +
			"&address=" + addressParam +
			"&algorithm=model-v3-narrow"

		// Call the API
		resp, err := http.Get(apiURL)
		if err != nil {
			fmt.Println("Error calling API for:", address, "-", err)
			continue
		}

		body, err := io.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			fmt.Println("Error reading API response for:", address, "-", err)
			continue
		}

		var result Response

		// Parse the JSON response into our struct
		if err := json.Unmarshal(body, &result); err != nil {
			fmt.Println("Error parsing JSON for:", address, "-", err)
			fmt.Println("Raw response:", string(body))
			continue
		}

		// If there is no SimKey, skip this record
		if result.SimKey == "" {
			continue
		}

		// Store the address and SimKey in memory for later clustering
		records = append(records, Record{
			Input:  address,
			SimKey: result.SimKey,
		})
	}

	// Check for any scan error from reading the file
	if err := scanner.Err(); err != nil {
		fmt.Println("Error reading input file:", err)
		return
	}

	if len(records) == 0 {
		fmt.Println("No records with similarity keys found.")
		return
	}

	//----------------------------------------------------------------------
	// Sort the records strictly by SimKey only.
	// This ensures that all identical SimKeys are adjacent in the slice,
	// making it easy to identify groups of matches.
	//----------------------------------------------------------------------
	sort.Slice(records, func(i, j int) bool {
		return records[i].SimKey < records[j].SimKey
	})

	//----------------------------------------------------------------------
	// Walk through the sorted slice and collect clusters of records that
	// share the same SimKey. Only print clusters that contain two or more
	// records. Each line is printed as "Input,SimKey" and clusters are
	// separated by a blank line.
	//----------------------------------------------------------------------

	currentKey := ""
	var cluster []Record

	// Helper function to print a cluster if it has two or more records
	printCluster := func(c []Record) {
		if len(c) < 2 {
			return
		}
		for _, r := range c {
			fmt.Printf("%s,%s\n", r.Input, r.SimKey)
		}
		fmt.Println() // blank line between clusters
	}

	for _, rec := range records {
		if rec.SimKey != currentKey {
			// SimKey changed: flush the previous cluster (if any)
			if len(cluster) > 0 {
				printCluster(cluster)
			}
			currentKey = rec.SimKey
			cluster = []Record{rec}
		} else {
			// Same SimKey as current cluster, add to it
			cluster = append(cluster, rec)
		}
	}

	// Flush the final cluster at the end
	if len(cluster) > 0 {
		printCluster(cluster)
	}
}
