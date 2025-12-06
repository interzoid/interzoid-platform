// example.java
//
// This example shows how to call Interzoid's Company Name Matching API
// using plain Java without any external libraries.
//
// It sends a request to the getcompanymatchadvanced endpoint and prints
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

    // Replace with your API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public static void main(String[] args) {
        // Construct the API URL using simple string concatenation.
        // Change the company or algorithm to match your needs.
        String apiUrl =
            "https://api.interzoid.com/getcompanymatchadvanced?license=" + API_KEY +
            "&company=ibm" +
            "&algorithm=model-v4-wide";

        HttpURLConnection connection = null;

        try {
            // Convert the string URL into a URI to avoid deprecation warnings.
            URI uri = new URI(apiUrl);

            // Convert URI to URL for opening an HTTP connection.
            URL url = uri.toURL();

            // Open an HTTP GET connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int statusCode = connection.getResponseCode();

            if (statusCode == HttpURLConnection.HTTP_OK) {
                // Read the response body into a single string
                String responseBody = readStream(connection.getInputStream());

                // A sample Interzoid JSON response looks like:
                // {
                //   "SimKey": "somevalue",
                //   "Code": "success",
                //   "Credits": "99"
                // }
                //
                // Since this beginner example uses no JSON libraries,
                // we extract fields by basic string searching.

                String simKey  = extractJsonValue(responseBody, "SimKey");
                String code    = extractJsonValue(responseBody, "Code");
                String credits = extractJsonValue(responseBody, "Credits");

                // Output values
                System.out.println("Match Similarity Key: " + simKey);
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

    // Reads the entire InputStream into a single string.
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

    // Extracts a simple JSON value using string operations.
    // Works for responses like: "Key": "Value"
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
