import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

// Input file containing one company name per line
const val INPUT_FILE_NAME = "sample-input-file.txt"

// Simple data class to hold the input company name and its similarity key (SimKey)
data class Record(
    val input: String,
    val simKey: String
)

fun main() {

    val records = mutableListOf<Record>()

    // Read the input file line by line
    val inputFile = File(INPUT_FILE_NAME)
    if (!inputFile.exists()) {
        println("Input file not found: $INPUT_FILE_NAME")
        return
    }

    inputFile.forEachLine { line ->
        val company = line.trim()

        // Skip blank lines
        if (company.isEmpty()) return@forEachLine

        val simKey = getCompanySimKey(company)

        // If no SimKey returned, skip this record
        if (simKey.isNullOrEmpty()) return@forEachLine

        records.add(Record(input = company, simKey = simKey))
    }

    if (records.isEmpty()) {
        println("No records with similarity keys found.")
        return
    }

    //----------------------------------------------------------------------
    // Sort records strictly by SimKey only so that matching SimKeys
    // are adjacent. This makes it easy to find clusters of matches.
    //----------------------------------------------------------------------
    records.sortBy { it.simKey }

    //----------------------------------------------------------------------
    // Walk through the sorted list and build clusters of records with
    // the same SimKey. Only print clusters containing two or more records.
    // Each printed line is formatted as "Input,SimKey" (two-column CSV)
    // and each cluster is separated by a blank line.
    //----------------------------------------------------------------------
    var currentKey: String? = null
    val cluster = mutableListOf<Record>()

    fun printCluster(c: List<Record>) {
        if (c.size < 2) return        // Only print clusters of size >= 2
        for (r in c) {
            println("${r.input},${r.simKey}")
        }
        println()                     // Blank line between clusters
    }

    for (rec in records) {
        if (rec.simKey != currentKey) {
            // New SimKey encountered: flush previous cluster
            if (cluster.isNotEmpty()) {
                printCluster(cluster)
                cluster.clear()
            }
            currentKey = rec.simKey
        }
        cluster.add(rec)
    }

    // Flush the last cluster at the end
    if (cluster.isNotEmpty()) {
        printCluster(cluster)
    }
}

/**
 * Calls Interzoid's getcompanymatchadvanced API for a single company name
 * and returns the similarity key (SimKey) as a String.
 * Returns null or empty if the call fails or no SimKey is present.
 */
fun getCompanySimKey(companyName: String): String? {
    // URL-encode the company so it is safe for use in a query parameter
    val companyParam = URLEncoder.encode(companyName, "UTF-8")

    // Build the API URL
    val apiUrl = "https://api.interzoid.com/getcompanymatchadvanced" +
            "?license=$API_KEY" +
            "&company=$companyParam" +
            "&algorithm=model-v4-wide"

    return try {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Read the response body as a String
        val body = connection.inputStream.bufferedReader().use { it.readText() }

        connection.disconnect()

        // For this simple example, we avoid external JSON libraries and
        // manually extract the "SimKey" value from the JSON response.
        // The response is expected to look like:
        // {"SimKey":"...","Code":"Success","Credits":"..."}
        extractSimKey(body)
    } catch (e: Exception) {
        println("Error calling API for \"$companyName\": ${e.message}")
        null
    }
}

/**
 * Minimal helper to extract the value of the "SimKey" field
 * from a simple JSON object string like:
 * {"SimKey":"...","Code":"Success","Credits":"..."}
 *
 * This avoids adding external JSON dependencies for a small example.
 * For production code, use a proper JSON library such as Jackson or kotlinx.serialization.
 */
fun extractSimKey(json: String): String? {
    val key = "\"SimKey\""
    val keyIndex = json.indexOf(key)
    if (keyIndex == -1) return null

    // Find the first quote after "SimKey":
    val colonIndex = json.indexOf(':', keyIndex)
    if (colonIndex == -1) return null

    val firstQuote = json.indexOf('"', colonIndex + 1)
    if (firstQuote == -1) return null

    val secondQuote = json.indexOf('"', firstQuote + 1)
    if (secondQuote == -1) return null

    return json.substring(firstQuote + 1, secondQuote)
}
