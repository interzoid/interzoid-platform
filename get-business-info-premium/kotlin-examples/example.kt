// example.kt
//
// This example calls the Interzoid Business Info API,
// extracts several fields from the JSON response, and prints them.
//
// Compile and run:
//   kotlinc example.kt -include-runtime -d example.jar
//   java -jar example.jar

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

fun main() {

    // Encode lookup text to handle spaces or punctuation safely.
    val lookup = URLEncoder.encode("Cisco", "UTF-8")

    // Build the API URL.
    val apiURL = "https://api.interzoid.com/getbusinessinfo" +
            "?license=$API_KEY" +
            "&lookup=$lookup"

    println("Calling Interzoid API...")
    println("URL: $apiURL")
    println()

    try {
        // Open HTTP connection and send GET request.
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Check for a successful response.
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read response into a String.
        val responseBuilder = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.forEachLine { line ->
                responseBuilder.append(line)
            }
        }
        val responseBody = responseBuilder.toString()

        // Optional: print raw JSON for reference.
        println("Raw JSON response:")
        println(responseBody)
        println()

        // Extract JSON fields manually.
        val name = extractJsonValue(responseBody, "CompanyName") ?: "N/A"
        val urlResponse = extractJsonValue(responseBody, "CompanyURL") ?: "N/A"
        val location = extractJsonValue(responseBody, "CompanyLocation") ?: "N/A"
        val description = extractJsonValue(responseBody, "CompanyDescription") ?: "N/A"
        val revenue = extractJsonValue(responseBody, "Revenue") ?: "N/A"
        val employees = extractJsonValue(responseBody, "NumberEmployees") ?: "N/A"
        val naics = extractJsonValue(responseBody, "NAICS") ?: "N/A"
        val topExec = extractJsonValue(responseBody, "TopExecutive") ?: "N/A"
        val topTitle = extractJsonValue(responseBody, "TopExecutiveTitle") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print selected values returned by the API.
        println("Company Name: $name")
        println("Website: $urlResponse")
        println("Location: $location")
        println("Description: $description")
        println("Revenue: $revenue")
        println("Employees: $employees")
        println("NAICS: $naics")
        println("Top Executive: $topExec")
        println("Top Executive Title: $topTitle")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Helper function to extract a JSON field manually,
 * without any external libraries.
 *
 * Looks for: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
