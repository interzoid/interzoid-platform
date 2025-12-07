// example.java
//
// This example shows how to call Interzoid's Parent Company Information API
// using plain Java with no external libraries.
//
// It sends a request to the getparentcompanyinfo endpoint and prints the
// company name, parent company, URLs, descriptions, and remaining credits.
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

    // Replace with your Interzoid API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public static void main(String[] args) {

        try {
            // Encode lookup value for safety (handles spaces, punctuation, etc.)
            String lookup = URLEncoder.encode("informatica", StandardCharsets.UTF_8.toString());

            // Build the API URL
            String apiUrl =
                "https://api.interzoid.com/getparentcompanyinfo?license=" + API_KEY +
                "&lookup=" + lookup;

            HttpURLConnection connection = null;

            try {
                // Convert to URI then URL to avoid deprecated constructor warnings
                URI uri = new URI(apiUrl);
                URL url = uri.toURL();

                // Open HTTP GET connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {

                    // Read the full JSON response into a string
                    String responseBody = readStream(connection.getInputStream());

                    // Example fields returned by this API:
                    //
                    // {
                    //   "CompanyName": "Informatica",
                    //   "CompanyURL": "https://www.informatica.com",
                    //   "ParentCompany": "Some Parent",
                    //   "ParentCompanyLocation": "California, USA",
                    //   "ParentCompanyURL": "https://...",
                    //   "ParentCompanyDescription": "Description text...",
                    //   "ParentCompanyReferenceURL": "https://...",
                    //   "Code": "success",
                    //   "Credits": "99"
                    // }

                    String companyName               = extractJsonValue(responseBody, "CompanyName");
                    String companyURL                = extractJsonValue(responseBody, "CompanyURL");
                    String parentCompany             = extractJsonValue(responseBody, "ParentCompany");
                    String parentLocation            = extractJsonValue(responseBody, "ParentCompanyLocation");
                    String parentURL                 = extractJsonValue(responseBody, "ParentCompanyURL");
                    String parentDescription         = extractJsonValue(responseBody, "ParentCompanyDescription");
                    String parentReferenceURL        = extractJsonValue(responseBody, "ParentCompanyReferenceURL");
                    String code                      = extractJsonValue(responseBody, "Code");
                    String credits                   = extractJsonValue(responseBody, "Credits");

                    // Output selected fields from the API response
                    System.out.println("Company Name: " + companyName);
                    System.out.println("Company URL: " + companyURL);
                    System.out.println("Parent Company: " + parentCompany);
                    System.out.println("Parent Location: " + parentLocation);
                    System.out.println("Parent Website: " + parentURL);
                    System.out.println("Parent Description: " + parentDescription);
                    System.out.println("Parent Reference URL: " + parentReferenceURL);
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

    // Helper: Read InputStream into a single String
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

    // Helper: Simple JSON field extractor using basic string operations
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
