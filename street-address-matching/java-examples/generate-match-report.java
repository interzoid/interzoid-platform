import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a list of street addresses from a text file, generates
 * similarity keys (SimKeys) using Interzoid's Street Address
 * Matching API (getaddressmatchadvanced), then groups and prints
 * addresses that share the same SimKey.
 *
 * Each printed line is "Input,SimKey" (two-column CSV style).
 * Only clusters with two or more matching addresses are printed.
 *
 * File name: generate-address-match-report.java
 * Run with:  java GenerateAddressMatchReport
 */
class GenerateAddressMatchReport {

    // Replace this with your API key from https://www.interzoid.com/manage-api-account
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    // Input file containing one street address per line
    private static final String INPUT_FILE_NAME = "sample-input-file.txt";

    // Holds one input address and its generated similarity key
    private static class Record {
        String input;
        String simKey;

        Record(String input, String simKey) {
            this.input = input;
            this.simKey = simKey;
        }
    }

    // Maps the JSON returned by the getaddressmatchadvanced API
    private static class ResponseData {
        String simKey;
        String code;
        String credits;
    }

    public static void main(String[] args) {
        List<Record> records = new ArrayList<>();

        // Open the input file for reading
        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_NAME))) {
            String address;

            // Read each line as a street address and call the API
            while ((address = reader.readLine()) != null) {
                // Skip blank lines
                if (address.trim().isEmpty()) {
                    continue;
                }

                ResponseData apiResult;
                try {
                    apiResult = callGetAddressMatchAdvanced(address);
                } catch (Exception e) {
                    System.out.println("Error calling API for: " + address + " - " + e.getMessage());
                    continue;
                }

                // If there is no SimKey, skip this record
                if (apiResult == null || apiResult.simKey == null || apiResult.simKey.isEmpty()) {
                    continue;
                }

                // Store the address and SimKey in memory for later clustering
                records.add(new Record(address, apiResult.simKey));
            }

        } catch (IOException e) {
            System.out.println("Error opening input file: " + e.getMessage());
            return;
        }

        // Check if we got any records with SimKeys
        if (records.isEmpty()) {
            System.out.println("No records with similarity keys found.");
            return;
        }

        //------------------------------------------------------------------
        // Sort the records strictly by SimKey only.
        // This ensures that all identical SimKeys are adjacent in the list,
        // making it easy to identify groups of matches.
        //------------------------------------------------------------------
        Collections.sort(records, Comparator.comparing(r -> r.simKey));

        //------------------------------------------------------------------
        // Walk through the sorted list and collect clusters of records that
        // share the same SimKey. Only print clusters that contain two or more
        // records. Each line is printed as "Input,SimKey" and clusters are
        // separated by a blank line.
        //------------------------------------------------------------------
        String currentKey = null;
        List<Record> cluster = new ArrayList<>();

        for (Record rec : records) {
            if (currentKey == null || !rec.simKey.equals(currentKey)) {
                // SimKey changed: flush the previous cluster (if any)
                printCluster(cluster);
                cluster.clear();

                currentKey = rec.simKey;
                cluster.add(rec);
            } else {
                // Same SimKey as current cluster, add to it
                cluster.add(rec);
            }
        }

        // Flush the final cluster at the end
        printCluster(cluster);
    }

    /**
     * Helper function to print a cluster if it has two or more records.
     * Each record is printed as "Input,SimKey", with a blank line between clusters.
     */
    private static void printCluster(List<Record> cluster) {
        if (cluster.size() < 2) {
            return;
        }
        for (Record r : cluster) {
            System.out.printf("%s,%s%n", r.input, r.simKey);
        }
        System.out.println(); // blank line between clusters
    }

    /**
     * Calls the getaddressmatchadvanced API for a given street address.
     */
    private static ResponseData callGetAddressMatchAdvanced(String address) throws IOException {
        String addressParam = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getaddressmatchadvanced?license=" + API_KEY +
            "&address=" + addressParam +
            "&algorithm=model-v3-narrow";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(
                status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream(),
                StandardCharsets.UTF_8
            )
        );

        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            responseBuilder.append(line);
        }
        in.close();
        conn.disconnect();

        String json = responseBuilder.toString().trim();
        if (json.isEmpty()) {
            return null;
        }

        ResponseData data = new ResponseData();
        data.simKey = extractJsonValue(json, "SimKey");
        data.code = extractJsonValue(json, "Code");
        data.credits = extractJsonValue(json, "Credits");

        return data;
    }

    /**
     * Minimal JSON value extractor for flat JSON like:
     * {"SimKey":"...","Code":"Success","Credits":"1"}
     * (No external libraries, to keep parity with the Go example.)
     */
    private static String extractJsonValue(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(quotedKey);
        if (keyIndex == -1) return "";

        int colonIndex = json.indexOf(':', keyIndex + quotedKey.length());
        if (colonIndex == -1) return "";

        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        // Handle quoted string value
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            valueStart++;
            StringBuilder sb = new StringBuilder();
            boolean escaping = false;

            for (int i = valueStart; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaping) {
                    sb.append(c);
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            // Fallback for non-string values (not expected here)
            int valueEnd = valueStart;
            while (valueEnd < json.length()
                    && json.charAt(valueEnd) != ','
                    && json.charAt(valueEnd) != '}'
                    && !Character.isWhitespace(json.charAt(valueEnd))) {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }
}
