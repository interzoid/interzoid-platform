// append-simkeys-to-file.kt
//
// Reads addresses from a text file (one per line),
// calls the Interzoid Address Match Advanced API for each,
// and writes original value + SimKey to a CSV file.
//
// Input:  sample-input-file.txt
// Output: output.csv
//
// Compile and run:
//   kotlinc append-simkeys-to-file.kt -include-runtime -d append-simkeys-to-file.jar
//   java -jar append-simkeys-to-file.jar

import java.io.File
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader

// Replace this with your API key from https://www.interzoid.com/manage-api-account
const val API_KEY = "YOUR_API_KEY_HERE"

// Hardcoded input and output file names
const val INPUT_FILE_NAME = "sample-input-file.txt"
const val OUTPUT_FILE_NAME = "output.csv"

fun main() {

    // Open the input file
    val inputFile = File(INPUT_FILE_NAME)
    if (!inputFile.exists()) {
        println("Error: input file \"$INPUT_FILE_NAME\" not found.")
        return
    }

    // Create (or overwrite) the output CSV file
    val outputFile = File(OUTPUT_FILE_NAME)

    PrintWriter(outputFile).use { writer ->
        var lineNumber = 0

        inputFile.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                val originalValue = line

                // Skip completely empty lines (optional)
                if (originalValue.isEmpty()) return@forEach

                // URL-encode the address to safely handle spaces and punctuation
                val encodedAddress = URLEncoder.encode(originalValue, "UTF-8")

                // Build the Address Match Advanced API URL
                val apiURL =
                    "https://api.interzoid.com/getaddressmatchadvanced" +
                            "?license=$API_KEY" +
                            "&address=$encodedAddress" +
                            "&algorithm=model-v3-narrow"

                try {
                    // Make the HTTP GET request
                    val url = URL(apiURL)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    // Read the response body as a single string
                    val responseBody = BufferedReader(
                        InputStreamReader(connection.inputStream)
                    ).use { it.readText() }

                    // Get the SimKey from the JSON response (no external JSON library)
                    val simKey = extractJsonValue(responseBody, "SimKey") ?: ""

                    // Write one CSV row: original address + SimKey
                    writeCsvRow(writer, listOf(originalValue, simKey))

                } catch (e: Exception) {
                    // On any error, log it and still write a row with an empty SimKey
                    println(
                        "API error on line $lineNumber " +
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
 * Extracts a simple JSON string value from a JSON response without using
 * any external libraries. Looks for: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}

/**
 * Writes a single CSV row, escaping commas and quotes safely.
 */
fun writeCsvRow(writer: PrintWriter, columns: List<String>) {
    val escapedColumns = columns.map { csvEscape(it) }
    writer.println(escapedColumns.joinToString(","))
}

/**
 * Escapes a CSV value by doubling any double-quotes and wrapping the value in quotes.
 */
fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

/**
 * Helper function for cleaner log output.
 */
fun quoteForLog(value: String): String = "\"$value\""
