// example.kt
//
// This example calls the Interzoid Email Trust Score API,
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

    // Encode the email, since email addresses often contain special characters.
    val email = URLEncoder.encode("billsmith11@gmail.com", "UTF-8")

    // Build the full URL string.
    val apiURL = "https://api.interzoid.com/emailtrustscore" +
            "?license=$API_KEY" +
            "&lookup=$email"

    println("Calling Interzoid API...")
    println("URL: $apiURL")
    println()

    try {
        // Open HTTP connection, send GET request
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Expect HTTP 200 response
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read API response into a String
        val responseBuilder = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.forEachLine { line ->
                responseBuilder.append(line)
            }
        }
        val responseBody = responseBuilder.toString()

        // Optional: print the raw JSON
        println("Raw JSON response:")
        println(responseBody)
        println()

        // Extract JSON fields manually
        val emailResponse = extractJsonValue(responseBody, "Email") ?: "N/A"
        val score = extractJsonValue(responseBody, "Score") ?: "N/A"
        val reasoning = extractJsonValue(responseBody, "Reasoning") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print values
        println("Email: $emailResponse")
        println("Trust Score: $score")
        println("Reasoning: $reasoning")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Helper function to extract a JSON field value
 * without using any external libraries.
 *
 * Looks for text like: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
