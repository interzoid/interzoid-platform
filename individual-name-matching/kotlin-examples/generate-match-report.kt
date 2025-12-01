import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

// Input file containing one full name per line
const val INPUT_FILE_NAME = "sample-input-file.txt"

// Simple data class to hold the input name and its similarity key (SimKey)
data class Record(
    val input: String,
    val simKey: String
)

fun main() {

    val records = mutableListOf<Record>()

    val inputFile = File(INPUT_FILE_NAME)
    if (!inputFile.exists()) {
        println("Input file not found: $INPUT_FILE_NAME")
        return
    }

    // Read each line as a full name and call the API
    inputFile.forEachLine { line ->
        val fullName = line.trim()

        // Skip blank lines
        if (fullName.isEmpty()) return@forEachLine

        val simKey = getFullNameSimKey(fullName)

        // Skip if no SimKey returned
        if (simKey.isNullOrEmpty()) return@forEachLine

        records.add(Record(input = fullName, simKey = simKey))
    }

    if (records.isEmpty()) {
        println("No records with similarity keys found.")
        return
    }

    //----------------------------------------------------------------------
    // Sort strictly by SimKey so identical keys are adjacent and easy to cluster
    //----------------------------------------------------------------------
    records.sortBy { it.simKey }

    //----------------------------------------------------------------------
    // Walk through the sorted list and build clusters by SimKey.
    // Only print clusters with two or more records.
    // Each line: "Input,SimKey"
    // Clusters separated by blank lines.
    //----------------------------------------------------------------------
    var currentKey: String? = null
    val cluster = mutableListOf<Record>()

    fun printCluster(c: List<Record>) {
        if (c.size < 2) return     // Only print clusters with 2+ records
        for (r in c) {
            println("${r.input},${r.simKey}")
        }
        println()                  // blank line between clusters
    }

    for (rec in records) {
        if (rec.simKey != currentKey) {
            if (cluster.isNotEmpty()) {
                printCluster(cluster)
                cluster.clear()
            }
            currentKey = rec.simKey
        }
        cluster.add(rec)
    }

    // Flush final cluster
    if (cluster.isNotEmpty()) {
        printCluster(cluster)
    }
}

/**
 * Calls Interzoid's getfullnamematch API for a single full name
 * and returns the similarity key (SimKey) as a String.
 * Returns null or empty if there is an error or no SimKey.
 */
fun getFullNameSimKey(fullName: String): String? {
    val encodedName = URLEncoder.encode(fullName, "UTF-8")

    val apiUrl = "https://api.interzoid.com/getfullnamematch" +
            "?license=$API_KEY" +
            "&fullname=$encodedName"

    return try {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val body = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        // For this example, manually extract "SimKey" from the JSON response.
        // Response is expected to look like:
        // {"SimKey":"...","Code":"Success","Credits":"..."}
        extractSimKey(body)
    } catch (e: Exception) {
        println("Error calling API for \"$fullName\": ${e.message}")
        null
    }
}

/**
 * Minimal helper to extract "SimKey" from a simple JSON object string.
 * For production, use a JSON library like Jackson or kotlinx.serialization.
 */
fun extractSimKey(json: String): String? {
    val key = "\"SimKey\""
    val keyIndex = json.indexOf(key)
    if (keyIndex == -1) return null

    val colonIndex = json.indexOf(':', keyIndex)
    if (colonIndex == -1) return null

    val firstQuote = json.indexOf('"', colonIndex + 1)
    if (firstQuote == -1) return null

    val secondQuote = json.indexOf('"', firstQuote + 1)
    if (secondQuote == -1) return null

    return json.substring(firstQuote + 1, secondQuote)
}
