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
 * Reads street addresses from a text file, calls Interzoid's Address Match
 * Advanced API, and writes original value + SimKey to a CSV file.
 *
 * File name: append-simkeys-to-file-address.java
 * Run with:  java AppendAddressSimkeysToFile
 */
class AppendAddressSimkeysToFile {

    // Replace with your API key: https://www.interzoid.com/manage-api-account
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    private static final String INPUT_FILE_NAME = "sample-input-file.txt";
    private static final String OUTPUT_FILE_NAME = "output.csv";

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

                // Skip blank lines if desired
                if (originalValue.length() == 0) {
                    continue;
                }

                ResponseData result;
                try {
                    result = callApiForAddress(originalValue);
                } catch (Exception e) {
                    System.out.printf(
                        "API error on line %d (\"%s\"): %s%n",
                        lineNumber, originalValue, e.getMessage()
                    );
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                if (result == null) {
                    writer.println(toCsvRow(originalValue, ""));
                    continue;
                }

                writer.println(toCsvRow(originalValue, result.simKey));
            }

            System.out.println("Done. Results written to " + OUTPUT_FILE_NAME);

        } catch (IOException e) {
            System.out.println("Error with input/output files: " + e.getMessage());
        }
    }

    /**
     * Calls getaddressmatchadvanced for a given street address.
     */
    private static ResponseData callApiForAddress(String address) throws IOException {
        String addrParam = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());

        String apiUrl =
            "https://api.interzoid.com/getaddressmatchadvanced?license=" + API_KEY +
            "&address=" + addrParam +
            "&algorithm=model-v3-narrow";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        BufferedReader in = new BufferedReader(
                new java.io.InputStreamReader(
                        status >= 200 && status < 300 ?
                        conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8));

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
     * Very small JSON extractor for flat key/value pairs.
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
     * Basic CSV escape function
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
