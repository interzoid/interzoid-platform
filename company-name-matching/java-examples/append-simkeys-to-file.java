import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Reads company names from a text file, calls Interzoid's company match API
 * for each line, and writes the original value + SimKey to a CSV file.
 *
 * File name: append-simkeys-to-file.java
 * Run with:  java AppendSimkeysToFile
 */
class AppendSimkeysToFile {

    // Replace this with your API key from https://www.interzoid.com/manage-api-account
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    // Hardcoded input and output file names (same as Go example)
    private static final String INPUT_FILE_NAME = "sample-input-file.txt";
    private static final String OUTPUT_FILE_NAME = "output.csv";

    // Simple POJO to hold parsed JSON response
    private static class ResponseData {
        String simKey;
        String code;
        String credits;
    }

    public static void main(String[] args) {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_NAME));
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME)))
        ) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String originalValue = line;

                // Skip completely empty lines (same as Go example)
                if (originalValue.length() == 0) {
                    continue;
                }

                ResponseData result;
                try {
                    result = callApiForCompany(originalValue);
                } catch (Exception e) {
                    System.out.printf("Error calling API for line %d (%q): %s%n",
                            lineNumber, originalValue, e.getMessage());
                    // On error, write original value and empty SimKey
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                if (result == null) {
                    System.out.printf("No response for line %d (%q)%n", lineNumber, originalValue);
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                // Optional: check result.code for "Success"
                if (!"Success".equalsIgnoreCase(result.code)) {
                    System.out.printf(
                        "Non-success code for line %d (%q): Code=%s%n",
                        lineNumber, originalValue, result.code
                    );
                }

                // Write original value and SimKey as a CSV row
                writer.println(toCsvRow(originalValue, result.simKey));
            }

            System.out.println("Done. Results written to " + OUTPUT_FILE_NAME);

        } catch (IOException e) {
            System.out.println("Error opening/creating files: " + e.getMessage());
        }
    }

    /**
     * Calls the Interzoid getcompanymatchadvanced API for a given company name.
     */
    private static ResponseData callApiForCompany(String companyName) throws IOException {
        String companyParam = URLEncoder.encode(companyName, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getcompanymatchadvanced?license=" + API_KEY +
            "&company=" + companyParam +
            "&algorithm=model-v4-wide";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        BufferedReader in;
        if (status >= 200 && status < 300) {
            in = new BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(
                    new java.io.InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder responseBuilder = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
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
     * Very simple JSON value extractor for flat JSON like:
     * {"SimKey":"...","Code":"Success","Credits":"1"}
     *
     * This avoids external JSON libraries.
     */
    private static String extractJsonValue(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(quotedKey);
        if (keyIndex == -1) {
            return "";
        }

        int colonIndex = json.indexOf(':', keyIndex + quotedKey.length());
        if (colonIndex == -1) {
            return "";
        }

        // Skip whitespace
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() &&
               Character.isWhitespace(json.charAt(valueStart))) {
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
            while (valueEnd < json.length() &&
                   json.charAt(valueEnd) != ',' &&
                   json.charAt(valueEnd) != '}' &&
                   !Character.isWhitespace(json.charAt(valueEnd))) {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }

    /**
     * Escapes a single field for CSV (RFC-4180-ish):
     * - Wrap in quotes if it contains comma, quote, or newline
     * - Double any internal quotes
     */
    private static String csvEscape(String field) {
        if (field == null) {
            field = "";
        }
        boolean mustQuote = field.contains(",") ||
                            field.contains("\"") ||
                            field.contains("\n") ||
                            field.contains("\r");
        if (!mustQuote) {
            return field;
        }
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Builds a single CSV row with two columns.
     */
    private static String toCsvRow(String col1, String col2) {
        return csvEscape(col1) + "," + csvEscape(col2);
    }
}
