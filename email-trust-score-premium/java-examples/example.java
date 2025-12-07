// example.java
//
// This example shows how to call Interzoid's Email Trust Score API
// using plain Java with no external libraries.
//
// It sends a request to the emailtrustscore endpoint and prints
// the email, score, reasoning, result code, and remaining credits.
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
            // Email addresses can include special characters,
            // so we URL-encode them for safety.
            String email = URLEncoder.encode("billsmith11@gmail.com", StandardCharsets.UTF_8.toString());

            // Construct API URL
            String apiUrl =
                "https://api.interzoid.com/emailtrustscore?license=" + API_KEY +
                "&lookup=" + email;

            HttpURLConnection connection = null;

            try {
                // Convert string URL to URI, then to URL
                URI uri = new URI(apiUrl);
                URL url = uri.toURL();

                // Open HTTP GET request
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {

                    // Read all response data
                    String responseBody = readStream(connection.getInputStream());

                    // Example JSON returned by API:
                    // {
                    //   "Email": "billsmith11@gmail.com",
                    //   "Score": "82",
                    //   "Reasoning": "Domain valid, age moderate",
                    //   "Code": "success",
                    //   "Credits": "99"
                    // }

                    String emailValue  = extractJsonValue(responseBody, "Email");
                    String score       = extractJsonValue(responseBody, "Score");
                    String reasoning   = extractJsonValue(responseBody, "Reasoning");
                    String code        = extractJsonValue(responseBody, "Code");
                    String credits     = extractJsonValue(responseBody, "Credits");

                    // Print API results
                    System.out.println("Email: " + emailValue);
                    System.out.println("Trust Score: " + score);
                    System.out.println("Reasoning: " + reasoning);
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

    // Read InputStream -> String
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

    // Simple JSON value extractor
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
