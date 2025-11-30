// example.kt
//
// This example reads company names from a text file (one per line),
// calls the Interzoid Company Match Advanced API for each line,
// and writes an output CSV with the original value and the returned SimKey.
//
// Input:  sample-input-file.txt   (one company name per line)
// Output: output.csv              (two columns: original, SimKey)
//
// Compile and run from the command line:
//   kotlinc example.kt -include-runtime -d example.jar
//   java -jar example.jar

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
    // Try to open the input file
    val inputFile = File(INPUT_FILE_NAME)
    if (!inputFile.exists()) {
        println("Error: input file \"$INPUT_FILE_NAME\" not found.")
        return
    }

    // Create (or overwrite) the output CSV file
    val outputFile = File(OUTPUT_FILE_NAME)

    PrintWriter(outputFile).use { writer ->
        var lineNumber = 0

        // Read the input file line by line
        inputFile.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                val originalValue = line

                // Skip completely empty lines (optional)
                if (originalValue.isEmpty()) {
                    return@forEach
                }

                // URL-encode the company name so it is safe for HTTP
                val companyParam = URLEncoder.encode(originalValue, "UTF-8")

                // Build the Interzoid API URL
                val apiURL =
                    "https://api.interzoid.com/getcompanymatchadvanced" +
                            "?license=$API_KEY" +
                            "&company=$companyParam" +
                            "&algorithm=model-v4-wide"

                try {
                    // Call the API
                    val url = URL(apiURL)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    // Read the response body into a string
                    val responseBuilder = StringBuilder()
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.forEachLine { responseLine ->
                            responseBuilder.append(responseLine)
                        }
                    }
                    val responseBody = responseBuilder.toString()

                    // Extract fields from the JSON response.
                    // We keep it simple and use a helper function instead of a JSON library.
                    val simKey = extractJsonValue(responseBody, "SimKey") ?: ""
                    val code = extractJsonValue(responseBody, "Code") ?: ""

                    // Optional: check result code
                    if (code != "Success") {
                        println(
                            "Non-success code for line $lineNumber (${quoteForLog(originalValue)}): " +
                                    "Code=$code"
                        )
                    }

                    // Write original value and SimKey as a CSV row
                    writeCsvRow(writer, listOf(originalValue, simKey))

                } catch (e: Exception) {
                    // On any error, log it and write a row with empty SimKey
                    println(
                        "Error processing line $lineNumber " +
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
 * Extracts a simple JSON string field from the given JSON text,
 * without using a JSON library.
 *
 * Looks for: "FieldName":"Value"
 */
fun extractJsonValue(json: String, fieldName: String): String? {
    val regex = """"$fieldName"\s*:\s*"([^"]*)"""".toRegex()
    val match = regex.find(json)
    return match?.groups?.get(1)?.value
}

/**
 * Writes a single CSV row. This simple helper:
 * - wraps each value in double-quotes
 * - escapes any existing double-quotes by doubling them
 *
 * This ensures commas and quotes are handled safely.
 */
fun writeCsvRow(writer: PrintWriter, columns: List<String>) {
    val escapedColumns = columns.map { csvEscape(it) }
    writer.println(escapedColumns.joinToString(","))
}

/**
 * Escapes a single CSV field by:
 * - doubling any double-quotes
 * - wrapping the entire field in double-quotes
 */
fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

/**
 * Helper used only for log messages (not for CSV).
 * Adds quotes around the value so it is clear in logs.
 */
fun quoteForLog(value: String): String = "\"$value\""
