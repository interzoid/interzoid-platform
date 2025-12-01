package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"sort"
)

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const API_KEY = "YOUR_API_KEY_HERE"

// Input file containing one company name per line
const inputFileName = "sample-input-file.txt"

func main() {

	// Record holds a single input company name and its generated similarity key (SimKey)
	type Record struct {
		Input  string
		SimKey string
	}

	// Response maps the JSON returned by the getcompanymatchadvanced API
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

	// Read the input file line by line, one company name per line
	for scanner.Scan() {
		company := scanner.Text()

		// Skip blank lines
		if company == "" {
			continue
		}

		// URL-encode the company name so it is safe to send as a URL parameter
		companyParam := url.QueryEscape(company)

		// Build the getcompanymatchadvanced API URL
		apiURL := "https://api.interzoid.com/getcompanymatchadvanced?license=" + API_KEY +
			"&company=" + companyParam +
			"&algorithm=model-v4-wide"

		// Call the API
		resp, err := http.Get(apiURL)
		if err != nil {
			fmt.Println("Error calling API for:", company, "-", err)
			continue
		}

		body, err := ioutil.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			fmt.Println("Error reading API response for:", company, "-", err)
			continue
		}

		var result Response

		// Parse the JSON response into our Response struct
		if err := json.Unmarshal(body, &result); err != nil {
			fmt.Println("Error parsing JSON for:", company, "-", err)
			fmt.Println("Raw response:", string(body))
			continue
		}

		// If there is no SimKey, skip this record
		if result.SimKey == "" {
			continue
		}

		// Store input and SimKey in memory for later sorting and clustering
		records = append(records, Record{
			Input:  company,
			SimKey: result.SimKey,
		})
	}

	// Check for any scan error from reading the file
	if err := scanner.Err(); err != nil {
		fmt.Println("Error reading input file:", err)
		return
	}

	// If there are no records, nothing to do
	if len(records) == 0 {
		fmt.Println("No records with similarity keys found.")
		return
	}

	//----------------------------------------------------------------------
	// Sort the records strictly by SimKey only.
	// This ensures that all identical SimKeys are adjacent in the slice,
	// which makes it easy to find matching clusters.
	//----------------------------------------------------------------------
	sort.Slice(records, func(i, j int) bool {
		return records[i].SimKey < records[j].SimKey
	})

	//----------------------------------------------------------------------
	// Walk through the sorted slice and collect clusters of records that
	// share the same SimKey. Only print clusters that contain two or more
	// records. Each line is printed as "Input,SimKey" to resemble a
	// two-column CSV file. Clusters are separated by a blank line.
	//----------------------------------------------------------------------

	currentKey := ""
	var cluster []Record

	// Helper function to print a cluster if it has two or more records
	printCluster := func(c []Record) {
		if len(c) < 2 {
			return
		}
		// Print each record in the cluster as "Input,SimKey"
		for _, r := range c {
			fmt.Printf("%s,%s\n", r.Input, r.SimKey)
		}
		// Blank line between clusters
		fmt.Println()
	}

	// Iterate through all records and build clusters based on SimKey
	for _, rec := range records {
		if rec.SimKey != currentKey {
			// When SimKey changes, flush the previous cluster (if any)
			if len(cluster) > 0 {
				printCluster(cluster)
			}
			// Start a new cluster
			currentKey = rec.SimKey
			cluster = []Record{rec}
		} else {
			// Same SimKey as current cluster, so add to it
			cluster = append(cluster, rec)
		}
	}

	// Flush the final cluster after the loop
	if len(cluster) > 0 {
		printCluster(cluster)
	}
}
