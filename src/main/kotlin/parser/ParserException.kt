package parser

fun prettyPrintParseError(
    source: String,
    errorOffset: Int,
    errorMessage: String,
): String {
    val lines = source.lines()

    var lineNumber = 1
    var column = errorOffset
    for (line in lines) {
        if (column <= line.length) {
            break
        }
        column -= line.length + 1 // +1 for the newline character
        lineNumber++
    }

    if (lineNumber > lines.size) {
        lineNumber = lines.size
        column = lines.last().length
    }

    val sb = StringBuilder("Syntax error at line $lineNumber, column $column: $errorMessage\n")

    val errorLine = if (lineNumber <= lines.size) lines[lineNumber - 1] else ""
    sb.appendLine(errorLine)

    val squiggly = " ".repeat(column) + "^" + "~".repeat(minOf(3, errorLine.length - column))
    sb.append(squiggly)
    return sb.toString()
}

class ParserException(
    msg: String,
    source: String,
    errorOffset: Int,
) : Exception(prettyPrintParseError(source, errorOffset, msg))
