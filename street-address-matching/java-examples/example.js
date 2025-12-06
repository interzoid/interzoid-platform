// example.java
//
// This example shows how to call Interzoid's Street Address Matching API
// using plain Java with no external libraries.
//
// It sends a request to the getaddressmatchadvanced endpoint and prints
// the similarity key, result code, and remaining credits.
//
// Register for an API key at:
// https://www.interzoid.com/manage-api-account

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class example {

    // Replace this with your API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public static void main(String[] args) {

        try {
            // Address must be URL-encoded because it contains spaces.
            String address = URLEncoder.encode("400 East Broadway St", StandardCharsets.UTF_8.toString());

            // Construct the API URL
            String apiUrl =
                "https://api.interzoid.com/getaddressmatchadvanced?license=" + API_KEY +
                "&address=" + address +
                "&algorithm=model-v3-narrow";

            HttpURLConnection connection = null;

            try {
                // Parse URL safely (avoids deprecated URL(String) constructor)
                URI uri = new URI(apiUrl);
                URL url = uri.toURL();

                // Open an HTTP GET connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {

                    // Read the response body
                    String responseBody = readStream(connection.getInputStream());

                    // Example JSON returned:
                    // {
                    //   "SimKey": "somevalue",
                    //   "Code": "success",
                    //   "Credits": "99"
                    // }

                    String simKey  = extractJsonValue(responseBody, "SimKey");
                    String code    = extractJsonValue(responseBody, "Code");
                    String credits = extractJsonValue(responseBody, "Credits");

                    // Output results
                    System.out.println("Address Similarity Key: " + simKey);
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

    // Reads the entire InputStream into a String
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

    // Extracts a JSON field's value using simple string operations
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
