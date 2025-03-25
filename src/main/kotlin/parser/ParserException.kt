package parser

import interactive.blue
import interactive.red
import interactive.redBold
import interactive.yellow

fun prettyPrintParseError(
    source: String,
    errorOffset: Int,
    errorMessage: String,
): String {
    val lines = source.lines()

    // Find the line and column of the error
    var lineNumber = 1
    var column = errorOffset
    for (line in lines) {
        if (column <= line.length) {
            break
        }
        column -= line.length + 1 // +1 for the newline character
        lineNumber++
    }

    // Ensure we don't go beyond the lines we have
    if (lineNumber > lines.size) {
        lineNumber = lines.size
        column = lines.last().length
    }

    // Print the error message with line and column
    val sb = StringBuilder("Syntax error at line $lineNumber, column $column:".redBold() + " $errorMessage\n".red())

    // Print the line with the error
    val errorLine = if (lineNumber <= lines.size) lines[lineNumber - 1].blue() else ""
    sb.appendLine(errorLine)

    // Print the squiggly line
    val squiggly = " ".repeat(column) + "^" + "~".repeat(minOf(3, errorLine.length - column)).yellow()
    sb.append(squiggly)
    return sb.toString()
}

class ParserException(
    msg: String,
    source: String,
    errorOffset: Int,
) : Exception(prettyPrintParseError(source, errorOffset, msg))
