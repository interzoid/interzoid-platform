// example.kt
//
// This is a very simple Kotlin example that calls the Interzoid
// Company Match Advanced API and prints a few values from the JSON response.
//
// To compile and run from the command line (from the folder containing this file):
//   kotlinc InterzoidCompanyMatch.kt -include-runtime -d InterzoidCompanyMatch.jar
//   java -jar InterzoidCompanyMatch.jar

import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

fun main() {

    // In this simple example, we hard-code the endpoint parameters.
    // You can change the company or algorithm as needed.
    val company = "ibm"
    val algorithm = "model-v4-wide"

    // Build the full URL string for the API call
    val urlString = "https://api.interzoid.com/getcompanymatchadvanced" +
            "?license=$API_KEY" +
            "&company=$company" +
            "&algorithm=$algorithm"

    println("Calling Interzoid API...")
    println("URL: $urlString")
    println()

    try {
        // Open an HTTP connection to the URL
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Check the HTTP status code
        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: $responseCode")
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

        // Optional: print the raw JSON so you can see what came back
        println("Raw JSON response:")
        println(responseBody)
        println()

        // The JSON response looks something like:
        // {
        //   "SimKey":"...",
        //   "Code":"Success",
        //   "Credits":"123456"
        // }
        //
        // To keep this example very simple and avoid extra libraries,
        // we pull out the fields with a small helper function that uses a regex.

        val simKey = extractJsonValue(responseBody, "SimKey") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print the values from the response
        println("Match Similarity Key: $simKey")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        // If anything goes wrong (network issue, bad URL, etc.), we show the error message
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Very small helper function to extract a simple JSON field value
 * from a JSON string, without using any third-party libraries.
 *
 * It looks for a pattern like:  "FieldName":"Value"
 * and returns the Value part, or null if it cannot be found.
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    // Example pattern: "SimKey":"some-value"
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
