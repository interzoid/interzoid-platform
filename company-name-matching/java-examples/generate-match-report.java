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
 * Reads company names from a text file, calls Interzoid's
 * getcompanymatchadvanced API to generate SimKeys, and prints
 * clusters of matching SimKeys (two or more records) as:
 *     Input,SimKey
 * with a blank line between clusters.
 *
 * Suggested file name: company-match-report.java
 * Run with:            java CompanyMatchReport
 */
class CompanyMatchReport {

    // Replace this with your API key from https://www.interzoid.com/manage-api-account
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    // Input file containing one company name per line
    private static final String INPUT_FILE_NAME = "sample-input-file.txt";

    // Holds a single input company name and its SimKey
    private static class Record {
        String input;
        String simKey;

        Record(String input, String simKey) {
            this.input = input;
            this.simKey = simKey;
        }
    }

    // Maps the JSON returned by the getcompanymatchadvanced API
    private static class ResponseData {
        String simKey;
        String code;
        String credits;
    }

    public static void main(String[] args) {
        List<Record> records = new ArrayList<>();

        // Read the input file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_NAME))) {
            String company;

            while ((company = reader.readLine()) != null) {
                // Skip blank lines
                if (company.trim().isEmpty()) {
                    continue;
                }

                // Call API and get SimKey
                ResponseData apiResult;
                try {
                    apiResult = callGetCompanyMatchAdvanced(company);
                } catch (Exception e) {
                    System.out.println("Error calling API for: " + company + " - " + e.getMessage());
                    continue;
                }

                if (apiResult == null || apiResult.simKey == null || apiResult.simKey.isEmpty()) {
                    // If there is no SimKey, skip this record
                    continue;
                }

                records.add(new Record(company, apiResult.simKey));
            }

        } catch (IOException e) {
            System.out.println("Error opening input file: " + e.getMessage());
            return;
        }

        // If there are no records, nothing to do
        if (records.isEmpty()) {
            System.out.println("No records with similarity keys found.");
            return;
        }

        //------------------------------------------------------------------
        // Sort the records strictly by SimKey only, so identical SimKeys
        // become adjacent and we can easily cluster.
        //------------------------------------------------------------------
        Collections.sort(records, Comparator.comparing(r -> r.simKey));

        //------------------------------------------------------------------
        // Walk through the sorted list and collect clusters of records that
        // share the same SimKey. Only print clusters that contain two or more
        // records. Each line is "Input,SimKey", with blank lines between clusters.
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
                // Same SimKey as current cluster, so add to it
                cluster.add(rec);
            }
        }

        // Flush final cluster
        printCluster(cluster);
    }

    /**
     * Helper to print a cluster if it has two or more records.
     * Each record is printed as: Input,SimKey
     * with a blank line between clusters.
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
     * Calls the getcompanymatchadvanced API for a given company name.
     * Returns ResponseData with SimKey, Code, Credits.
     */
    private static ResponseData callGetCompanyMatchAdvanced(String company) throws IOException {
        String companyParam = URLEncoder.encode(company, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getcompanymatchadvanced?license=" + API_KEY +
            "&company=" + companyParam +
            "&algorithm=model-v4-wide";

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
     * This avoids external libraries to keep it parallel to the Go example.
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
