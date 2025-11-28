// example.kt
//
// This example calls the Interzoid Custom Data Enrichment API,
// extracts fields from the JSON response, and prints them.
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

    // URL-encode each parameter because of punctuation and spaces
    val topic = URLEncoder.encode("detailed information about companies", "UTF-8")
    val lookup = URLEncoder.encode("IBM", "UTF-8")
    val output = URLEncoder.encode("""["headquarters", "ceo", "website","number of employees","ticker symbol","2023 revenue","2022 revenue"]""", "UTF-8")

    // Build the API URL
    val apiURL = "https://api.interzoid.com/getcustom" +
            "?license=$API_KEY" +
            "&topic=$topic" +
            "&lookup=$lookup" +
            "&model=default" +
            "&output=$output"

    println("Calling Interzoid API...")
    println("URL: $apiURL")
    println()

    try {
        // Open HTTP connection
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Verify HTTP 200 success
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read the raw JSON body
        val responseBuilder = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.forEachLine { line ->
                responseBuilder.append(line)
            }
        }
        val responseBody = responseBuilder.toString()

        // Optional: print full JSON
        println("Raw JSON response:")
        println(responseBody)
        println()

        // Extract JSON keys manually
        val headquarters = extractJsonValue(responseBody, "headquarters") ?: "N/A"
        val ceo = extractJsonValue(responseBody, "ceo") ?: "N/A"
        val website = extractJsonValue(responseBody, "website") ?: "N/A"
        val employees = extractJsonValue(responseBody, "number of employees") ?: "N/A"
        val ticker = extractJsonValue(responseBody, "ticker symbol") ?: "N/A"
        val revenue2023 = extractJsonValue(responseBody, "2023 revenue") ?: "N/A"
        val revenue2022 = extractJsonValue(responseBody, "2022 revenue") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print formatted results
        println("Headquarters: $headquarters")
        println("CEO: $ceo")
        println("Website: $website")
        println("Employees: $employees")
        println("Ticker Symbol: $ticker")
        println("2023 Revenue: $revenue2023")
        println("2022 Revenue: $revenue2022")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Simple helper function to extract a JSON value without external
 * libraries. Searches for: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
