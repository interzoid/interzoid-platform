// append-simkeys-to-file.kt
//
// Reads names from a text file, one per line,
// calls the Interzoid Full Name Match API for each,
// and writes an output CSV containing the original name and the SimKey.
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

                // Skip empty lines (optional)
                if (originalValue.isEmpty()) return@forEach

                // URL encode the name so it works safely
                val nameParam = URLEncoder.encode(originalValue, "UTF-8")

                // Build the API URL
                val apiURL =
                    "https://api.interzoid.com/getfullnamematch" +
                            "?license=$API_KEY" +
                            "&fullname=$nameParam"

                try {
                    // Make the GET request
                    val url = URL(apiURL)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    // Read body into a string
                    val responseBuilder = StringBuilder()
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.forEachLine { responseLine ->
                            responseBuilder.append(responseLine)
                        }
                    }
                    val responseBody = responseBuilder.toString()

                    // Extract fields without any JSON library
                    val simKey = extractJsonValue(responseBody, "SimKey") ?: ""

                    // Write one row to CSV
                    writeCsvRow(writer, listOf(originalValue, simKey))

                } catch (e: Exception) {
                    println(
                        "API error on line $lineNumber " +
                                "(${quoteForLog(originalValue)}): ${e.message}"
                    )
                    // Write blank SimKey so row count remains consistent
                    writeCsvRow(writer, listOf(originalValue, ""))
                }
            }
        }
    }

    println("Done. Results written to $OUTPUT_FILE_NAME")
}

/**
 * Extracts a JSON field like "FieldName":"Value"
 * without external libraries.
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}

/**
 * Writes one CSV row, escaping commas/quotes safely.
 */
fun writeCsvRow(writer: PrintWriter, columns: List<String>) {
    val escapedColumns = columns.map { csvEscape(it) }
    writer.println(escapedColumns.joinToString(","))
}

/**
 * Escapes CSV safely by doubling quotes and wrapping with quotes.
 */
fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

/**
 * Only used for log messages (not CSV).
 */
fun quoteForLog(value: String): String = "\"$value\""
