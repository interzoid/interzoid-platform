// example.kt
//
// This simple example calls the Interzoid Full Name Match API,
// extracts a few fields from the JSON response, and prints them.
//
// Compile and run from the command line:
//   kotlinc example.kt -include-runtime -d example.jar
//   java -jar example.jar

import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

fun main() {

    // Hard-coded example parameters.
    // You can change the full name as you like.
    val fullName = "James%20Johnston"

    // Build the URL string
    val urlString = "https://api.interzoid.com/getfullnamematch" +
            "?license=$API_KEY" +
            "&fullname=$fullName"

    println("Calling Interzoid API...")
    println("URL: $urlString")
    println()

    try {
        // Open connection and send HTTP GET request
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Check for success (HTTP 200)
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read the response into a String
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

        // Response format looks like:
        // {
        //   "SimKey":"...",
        //   "Code":"Success",
        //   "Credits":"12345"
        // }

        // Extract three fields manually (no library needed)
        val simKey = extractJsonValue(responseBody, "SimKey") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print the extracted values
        println("Similarity Key: $simKey")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        // Print any error (network or parsing)
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Helper function to extract JSON values without external libraries.
 * It looks for a pattern like:  "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
