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
 * Reads full names from a text file, calls Interzoid's Full Name Matching API
 * for each line, and writes the original value + SimKey to a CSV file.
 *
 * File name: append-simkeys-to-file-fullname.java
 * Run with:  java AppendFullnameSimkeysToFile
 */
class AppendFullnameSimkeysToFile {

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

                // Skip completely empty lines (same behavior as Go)
                if (originalValue.length() == 0) {
                    continue;
                }

                ResponseData result;
                try {
                    result = callApiForFullname(originalValue);
                } catch (Exception e) {
                    System.out.printf(
                        "API error on line %d (\"%s\"): %s%n",
                        lineNumber, originalValue, e.getMessage()
                    );
                    // On error, write original value and empty SimKey
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                if (result == null) {
                    System.out.printf(
                        "No response for line %d (\"%s\")%n",
                        lineNumber, originalValue
                    );
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                // (Optional) You could inspect result.code here if desired

                // Write original value and SimKey as a CSV row
                writer.println(toCsvRow(originalValue, result.simKey));
            }

            System.out.println("Done. Results written to " + OUTPUT_FILE_NAME);

        } catch (IOException e) {
            System.out.println("Error opening/creating files: " + e.getMessage());
        }
    }

    /**
     * Calls the Interzoid getfullnamematch API for a given full name.
     */
    private static ResponseData callApiForFullname(String fullName) throws IOException {
        String nameParam = URLEncoder.encode(fullName, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getfullnamematch?license=" + API_KEY +
            "&fullname=" + nameParam;

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
     * Escapes a single field for CSV:
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
