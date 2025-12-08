import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a list of individual names from a text file, generates
 * similarity keys (SimKeys) using Interzoid's Individual Name
 * Matching API (getfullnamematch), then groups and prints names
 * that share the same SimKey.
 *
 * Each printed line is "Input,SimKey" (two-column CSV style).
 * Only clusters with two or more matching names are printed.
 *
 * File name: generate-match-report.java
 * Run with:  java GenerateMatchReport
 */
class GenerateMatchReport {

    // Replace this with your API key from https://www.interzoid.com/manage-api-account
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    // Input file containing one full name per line
    private static final String INPUT_FILE_NAME = "sample-input-file.txt";

    // Holds one input name and its generated similarity key
    private static class Record {
        String input;
        String simKey;

        Record(String input, String simKey) {
            this.input = input;
            this.simKey = simKey;
        }
    }

    // Maps the JSON returned by the getfullnamematch API
    private static class ResponseData {
        String simKey;
        String code;
        String credits;
    }

    public static void main(String[] args) {
        List<Record> records = new ArrayList<>();

        // Open and read the input file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_NAME))) {
            String name;

            while ((name = reader.readLine()) != null) {
                // Skip blank lines
                if (name.trim().isEmpty()) {
                    continue;
                }

                ResponseData apiResult;
                try {
                    apiResult = callGetFullNameMatch(name);
                } catch (Exception e) {
                    System.out.println("Error calling API for: " + name + " - " + e.getMessage());
                    continue;
                }

                if (apiResult == null || apiResult.simKey == null || apiResult.simKey.isEmpty()) {
                    // If there is no SimKey, skip this record
                    continue;
                }

                // Store the name and SimKey for later clustering
                records.add(new Record(name, apiResult.simKey));
            }

        } catch (IOException e) {
            System.out.println("Error opening input file: " + e.getMessage());
            return;
        }

        // No valid records with SimKeys
        if (records.isEmpty()) {
            System.out.println("No records with similarity keys found.");
            return;
        }

        //------------------------------------------------------------------
        // Sort the records strictly by SimKey only so identical keys are
        // adjacent, making clustering easy.
        //------------------------------------------------------------------
        Collections.sort(records, Comparator.comparing(r -> r.simKey));

        //------------------------------------------------------------------
        // Walk through the sorted list and collect clusters of records that
        // share the same SimKey. Only print clusters that contain two or more
        // records. Each line is "Input,SimKey" and clusters are separated by
        // a blank line.
        //------------------------------------------------------------------
        String currentKey = null;
        List<Record> cluster = new ArrayList<>();

        for (Record rec : records) {
            if (currentKey == null || !rec.simKey.equals(currentKey)) {
                // SimKey changed: flush previous cluster
                printCluster(cluster);
                cluster.clear();

                currentKey = rec.simKey;
                cluster.add(rec);
            } else {
                // Same SimKey as current cluster, add to it
                cluster.add(rec);
            }
        }

        // Flush final cluster
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
        System.out.println();
    }

    /**
     * Calls the getfullnamematch API for a given full name.
     */
    private static ResponseData callGetFullNameMatch(String fullName) throws IOException {
        String nameParam = URLEncoder.encode(fullName, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getfullnamematch?license=" + API_KEY +
            "&fullname=" + nameParam;

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
     * (No external libraries, to keep it parallel to the Go example.)
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
