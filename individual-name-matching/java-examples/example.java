// example.java
//
// This example shows how to call Interzoid's Individual Name Matching API
// using plain Java with no external libraries.
//
// It sends a request to the getfullnamematch endpoint and prints
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
import java.net.URISyntaxException;
import java.net.URL;

public class example {

    // Replace this with your API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public static void main(String[] args) {

        // The API URL with a sample full name.
        // You can change "James Johnston" to any name you wish to test.
        String apiUrl =
            "https://api.interzoid.com/getfullnamematch?license=" + API_KEY +
            "&fullname=James%20Johnston";

        HttpURLConnection connection = null;

        try {
            // Convert the string into a URI, then to a URL
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();

            // Open HTTP connection
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
                //
                // We keep this example extremely simple by extracting fields
                // manually using basic string operations.

                String simKey  = extractJsonValue(responseBody, "SimKey");
                String code    = extractJsonValue(responseBody, "Code");
                String credits = extractJsonValue(responseBody, "Credits");

                // Output results
                System.out.println("Similarity Key: " + simKey);
                System.out.println("Result Code: " + code);
                System.out.println("Remaining Credits: " + credits);

            } else {
                System.out.println("API returned HTTP status code: " + statusCode);
            }

        } catch (IOException | URISyntaxException e) {
            System.out.println("Error calling Interzoid API: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Reads an InputStream into a single string.
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

    // Extracts a simple JSON value using basic string searching.
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
