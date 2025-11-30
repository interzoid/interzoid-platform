// standardize-file.kt
//
// Reads organization names from a text file (one per line),
// calls the Interzoid Organization Standardization API,
// and writes original value + Standard to a CSV file.
//
// Input:  sample-input-file.txt
// Output: output.csv
//
// Compile and run:
//   kotlinc standardize-file.kt -include-runtime -d standardize-file.jar
//   java -jar standardize-file.jar

import java.io.File
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader

// Replace with your key: https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

const val INPUT_FILE_NAME = "sample-input-file.txt"
const val OUTPUT_FILE_NAME = "output.csv"

fun main() {

    val inputFile = File(INPUT_FILE_NAME)
    if (!inputFile.exists()) {
        println("Error: input file \"$INPUT_FILE_NAME\" not found.")
        return
    }

    val outputFile = File(OUTPUT_FILE_NAME)

    PrintWriter(outputFile).use { writer ->
        var lineNumber = 0

        inputFile.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                val originalValue = line

                // Skip blank lines
                if (originalValue.isEmpty()) return@forEach

                // URL encode organization name
                val orgParam = URLEncoder.encode(originalValue, "UTF-8")

                // Build the API request
                val apiURL =
                    "https://api.interzoid.com/getorgstandard" +
                            "?license=$API_KEY" +
                            "&org=$orgParam"

                try {
                    // Open HTTP connection
                    val url = URL(apiURL)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    // Read response as one string
                    val responseBody = BufferedReader(
                        InputStreamReader(connection.inputStream)
                    ).use { it.readText() }

                    // Extract "Standard" from JSON manually
                    val standard = extractJsonValue(responseBody, "Standard") ?: ""

                    // Write output row
                    writeCsvRow(writer, listOf(originalValue, standard))

                } catch (e: Exception) {
                    println(
                        "API call error on line $lineNumber " +
                                "(${quoteForLog(originalValue)}): ${e.message}"
                    )
                    writeCsvRow(writer, listOf(originalValue, ""))
                }
            }
        }
    }

    println("Done. Results written to $OUTPUT_FILE_NAME")
}

/**
 * Very simple JSON extraction helper.
 * No external libraries used.
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    return regex.find(json)?.groups?.get(1)?.value
}

/**
 * Writes a row of CSV safely escaped.
 */
fun writeCsvRow(writer: PrintWriter, columns: List<String>) {
    val escaped = columns.map { csvEscape(it) }
    writer.println(escaped.joinToString(","))
}

/**
 * Escapes double-quotes and wraps field in quotes.
 */
fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

/**
 * Cleaner logging helper.
 */
fun quoteForLog(value: String): String = "\"$value\""
