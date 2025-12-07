// example.java
//
// This example shows how to call Interzoid's Business Information API
// using plain Java with no external libraries.
//
// It sends a request to the getbusinessinfo endpoint and prints core
// fields such as company name, URL, location, description, revenue,
// employee count, NAICS classification, top executive, and remaining credits.
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

    // Replace this with your Interzoid API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public static void main(String[] args) {

        try {
            // Encode lookup value so it works for any input
            String lookup = URLEncoder.encode("Cisco", StandardCharsets.UTF_8.toString());

            // Build the API endpoint URL
            String apiUrl =
                "https://api.interzoid.com/getbusinessinfo?license=" + API_KEY +
                "&lookup=" + lookup;

            HttpURLConnection connection = null;

            try {
                // Convert string → URI → URL (avoids deprecated URL(String))
                URI uri = new URI(apiUrl);
                URL url = uri.toURL();

                // Prepare HTTP GET request
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {

                    // Read API response body
                    String responseBody = readStream(connection.getInputStream());

                    // Example JSON returned by the API:
                    //
                    // {
                    //   "CompanyName": "Cisco Systems",
                    //   "CompanyURL": "https://www.cisco.com",
                    //   "CompanyLocation": "San Jose, CA",
                    //   "CompanyDescription": "...",
                    //   "Revenue": "51000000000",
                    //   "NumberEmployees": "82500",
                    //   "NAICS": "334210",
                    //   "TopExecutive": "Chuck Robbins",
                    //   "TopExecutiveTitle": "CEO",
                    //   "Code": "success",
                    //   "Credits": "99"
                    // }

                    String companyName        = extractJsonValue(responseBody, "CompanyName");
                    String companyURL         = extractJsonValue(responseBody, "CompanyURL");
                    String companyLocation    = extractJsonValue(responseBody, "CompanyLocation");
                    String description        = extractJsonValue(responseBody, "CompanyDescription");
                    String revenue            = extractJsonValue(responseBody, "Revenue");
                    String employees          = extractJsonValue(responseBody, "NumberEmployees");
                    String naics              = extractJsonValue(responseBody, "NAICS");
                    String topExec            = extractJsonValue(responseBody, "TopExecutive");
                    String topExecTitle       = extractJsonValue(responseBody, "TopExecutiveTitle");
                    String code               = extractJsonValue(responseBody, "Code");
                    String credits            = extractJsonValue(responseBody, "Credits");

                    // Print results in a readable format
                    System.out.println("Company Name: " + companyName);
                    System.out.println("Website: " + companyURL);
                    System.out.println("Location: " + companyLocation);
                    System.out.println("Description: " + description);
                    System.out.println("Revenue: " + revenue);
                    System.out.println("Employees: " + employees);
                    System.out.println("NAICS: " + naics);
                    System.out.println("Top Executive: " + topExec);
                    System.out.println("Top Executive Title: " + topExecTitle);
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

    // Reads full InputStream → String
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

    // Simple JSON field extractor using text search
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
