// example.java
//
// This example shows how to call Interzoid's Organization Standardization API
// using plain Java with no external libraries.
//
// It sends a request to the getorgstandard endpoint and prints
// the standardized organization name, result code, and remaining credits.
//
// Register for an API key at:
// https://www.interzoid.com/manage-api-account

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class example {

    // Replace this with your API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public static void main(String[] args) {

        try {
            // Organization names may contain punctuation or spaces,
            // so we URL-encode them for safety.
            String org = URLEncoder.encode("b.o.a.", StandardCharsets.UTF_8.toString());

            // Construct the API URL
            String apiUrl =
                "https://api.interzoid.com/getorgstandard?license=" + API_KEY +
                "&org=" + org;

            HttpURLConnection connection = null;

            try {
                // Parse URL safely (avoids deprecated URL(String) constructor)
                URI uri = new URI(apiUrl);
                URL url = uri.toURL();

                // Open HTTP connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {

                    // Read response into a single String
                    String responseBody = readStream(connection.getInputStream());

                    // Example JSON returned:
                    // {
                    //   "Standard": "Bank of America",
                    //   "Code": "success",
                    //   "Credits": "99"
                    // }

                    String standard = extractJsonValue(responseBody, "Standard");
                    String code     = extractJsonValue(responseBody, "Code");
                    String credits  = extractJsonValue(responseBody, "Credits");

                    // Print results
                    System.out.println("Standardized Organization: " + standard);
                    System.out.println("Result Code: " + code);
                    System.out.println("Remaining Credits: " + credits);

                } else {
                    System.out.println("API returned HTTP status code: " + statusCode);
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        } catch (IOException | URISyntaxException e) {
            System.out.println("Error calling Interzoid API: " + e.getMessage());
        }
    }

    // Helper method to read an InputStream into one String
    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();
        return builder.toString();
    }

    // A simple JSON field extractor using string searching.
    // Works for predictable API responses.
    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";

        int keyIndex = json.indexOf(pattern);
        if (keyIndex == -1) return "";

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return "";

        int firstQuote = json.indexOf("\"", colonIndex + 1);
        if (firstQuote == -1) return "";

        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return "";

        return json.substring(firstQuote + 1, secondQuote);
    }
}
