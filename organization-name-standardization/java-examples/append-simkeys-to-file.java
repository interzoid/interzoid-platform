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
 * Reads organization names from a text file, calls Interzoid's
 * getorgstandard API, and writes original value + Standard to a CSV file.
 *
 * File name: standardize-file.java
 * Run with:  java StandardizeFile
 */
class StandardizeFile {

    // Replace with your key: https://www.interzoid.com/manage-api-account
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    private static final String INPUT_FILE_NAME = "sample-input-file.txt";
    private static final String OUTPUT_FILE_NAME = "output.csv";

    // Mapping of JSON response
    private static class ResponseData {
        String standard;
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

                // Allow blank lines to be skipped
                if (originalValue.length() == 0) {
                    continue;
                }

                ResponseData result;
                try {
                    result = callApiForOrg(originalValue);
                } catch (Exception e) {
                    System.out.printf(
                        "API call error for line %d (\"%s\"): %s%n",
                        lineNumber, originalValue, e.getMessage()
                    );
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                if (result == null) {
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                // Write row to CSV: original value + Standard
                writer.println(toCsvRow(originalValue, result.standard));
            }

            System.out.println("Done. Results written to " + OUTPUT_FILE_NAME);

        } catch (IOException e) {
            System.out.println("Error opening/creating files: " + e.getMessage());
        }
    }

    /**
     * Calls the getorgstandard API for a given organization name.
     */
    private static ResponseData callApiForOrg(String orgName) throws IOException {
        String orgParam = URLEncoder.encode(orgName, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getorgstandard?license=" + API_KEY +
            "&org=" + orgParam;

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        BufferedReader in = new BufferedReader(
            new java.io.InputStreamReader(
                status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream(),
                StandardCharsets.UTF_8
            )
        );

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
        data.standard = extractJsonValue(json, "Standard");
        data.code = extractJsonValue(json, "Code");
        data.credits = extractJsonValue(json, "Credits");

        return data;
    }

    /**
     * Simple JSON extractor for flat key/value JSON like:
     * {"Standard":"...","Code":"Success","Credits":"1"}
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

        // Handle quoted string values
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
            // Fallback for non-string values (not really expected here)
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
     * Escapes a single field for CSV.
     */
    private static String csvEscape(String field) {
        if (field == null) field = "";
        boolean mustQuote = field.contains(",") ||
                            field.contains("\"") ||
                            field.contains("\n") ||
                            field.contains("\r");

        if (!mustQuote) return field;

        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    /**
     * Builds a two-column CSV row.
     */
    private static String toCsvRow(String col1, String col2) {
        return csvEscape(col1) + "," + csvEscape(col2);
    }
}
