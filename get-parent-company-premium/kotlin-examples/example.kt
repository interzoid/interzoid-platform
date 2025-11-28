// example.kt
//
// This example calls the Interzoid Parent Company Info API,
// extracts fields from the returned JSON, and prints them.
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

    // URL-encode lookup in case it contains spaces or punctuation.
    val lookup = URLEncoder.encode("informatica", "UTF-8")

    // Build the API URL.
    val apiURL = "https://api.interzoid.com/getparentcompanyinfo" +
            "?license=$API_KEY" +
            "&lookup=$lookup"

    println("Calling Interzoid API...")
    println("URL: $apiURL")
    println()

    try {
        // Open HTTP connection
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Expect success HTTP response
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Non-success HTTP response: ${connection.responseCode}")
            return
        }

        // Read response into a string
        val responseBuilder = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.forEachLine { line ->
                responseBuilder.append(line)
            }
        }
        val responseBody = responseBuilder.toString()

        // Optional: print raw JSON
        println("Raw JSON response:")
        println(responseBody)
        println()

        // Extract JSON values manually.
        val name = extractJsonValue(responseBody, "CompanyName") ?: "N/A"
        val urlResponse = extractJsonValue(responseBody, "CompanyURL") ?: "N/A"
        val parent = extractJsonValue(responseBody, "ParentCompany") ?: "N/A"
        val location = extractJsonValue(responseBody, "ParentCompanyLocation") ?: "N/A"
        val parentURL = extractJsonValue(responseBody, "ParentCompanyURL") ?: "N/A"
        val description = extractJsonValue(responseBody, "ParentCompanyDescription") ?: "N/A"
        val referenceURL = extractJsonValue(responseBody, "ParentCompanyReferenceURL") ?: "N/A"
        val code = extractJsonValue(responseBody, "Code") ?: "N/A"
        val credits = extractJsonValue(responseBody, "Credits") ?: "N/A"

        // Print selected values
        println("Company Name: $name")
        println("Company URL: $urlResponse")
        println("Parent Company: $parent")
        println("Parent Location: $location")
        println("Parent Website: $parentURL")
        println("Parent Description: $description")
        println("Parent Reference URL: $referenceURL")
        println("Result Code: $code")
        println("Remaining Credits: $credits")

    } catch (e: Exception) {
        println("Error calling Interzoid API: ${e.message}")
    }
}

/**
 * Small helper to extract a JSON string field from a response,
 * without external parsing libraries.
 *
 * Looks for: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}
