// example.kt
//
// This sample calls the Interzoid Organization Standardization API,
// reads the JSON response, and prints a few fields.
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

    // The organization contains punctuation, so we URL-encode it.
    val org = URLEncoder.encode("b.o.a.", "UTF-8")

    // Build the full API URL
    val apiURL = "https://api.interzoid.com/getorgstandard" +
            "?license=$API_KEY" +
            "&org=$org"

    println("Calling Interzoid API...")
    println("URL: $apiURL")
    println()

    try {
        // Open the HTTP connection
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Confirm HTTP 200 success
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read the API response into a string
        val responseBuilder = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.forEachLine { line ->
                responseBuilder.append(line)
            }
        }
        val responseBody = responseBuilder.toString()

        // Optional: show raw response
        println("Raw JSON response:")
        println(responseBody)
        println()

        // Extract JSON fields manually
        val standard = extractJsonValue(responseBody, "Standard") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print the processed output
        println("Standardized Organization: $standard")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Helper to extract a string value from JSON without external libraries.
 * Looks for a pattern like: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
