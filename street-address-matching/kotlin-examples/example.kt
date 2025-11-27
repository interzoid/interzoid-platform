// example.kt
//
// This example calls the Interzoid Address Match Advanced API,
// parses a few fields from the response JSON, and prints them.
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

    // Encode the address because it contains spaces.
    // URLEncoder is built into the standard library.
    val address = URLEncoder.encode("400 East Broadway St", "UTF-8")

    // Construct the full API request URL.
    val apiURL = "https://api.interzoid.com/getaddressmatchadvanced" +
            "?license=$API_KEY" +
            "&address=$address" +
            "&algorithm=model-v3-narrow"

    println("Calling Interzoid API...")
    println("URL: $apiURL")
    println()

    try {
        // Open an HTTP connection
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Ensure HTTP status code 200 (OK)
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read the response body into a String
        val responseBuilder = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.forEachLine { line ->
                responseBuilder.append(line)
            }
        }
        val responseBody = responseBuilder.toString()

        // Optional: print raw JSON response
        println("Raw JSON response:")
        println(responseBody)
        println()

        // Extract JSON values manually using a helper function.
        val simKey = extractJsonValue(responseBody, "SimKey") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print the values returned by the API
        println("Address Similarity Key: $simKey")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Helper function to extract a string field from JSON manually,
 * without any external dependency.
 *
 * It looks for patterns like: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
