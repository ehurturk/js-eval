package parser

/*
 * A parser combinator approach for parsing.
 */
data class ParseResult<T>(
    val value: T,
    val position: Int,
)

abstract class ParserC<T> {
    abstract fun parse(
        input: String,
        position: Int,
    ): ParseResult<T>?

    fun <R> map(transform: (T) -> R): ParserC<R> =
        object : ParserC<R>() {
            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<R>? {
                val result = this@ParserC.parse(input, position) ?: return null
                return ParseResult(transform(result.value), result.position)
            }
        }

    infix fun <R> and(other: ParserC<R>): ParserC<Pair<T, R>> =
        object : ParserC<Pair<T, R>>() {
            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<Pair<T, R>>? {
                val firstResult = this@ParserC.parse(input, position) ?: return null
                val secondResult = other.parse(input, firstResult.position) ?: return null
                return ParseResult(Pair(firstResult.value, secondResult.value), secondResult.position)
            }
        }

    infix fun or(other: ParserC<T>): ParserC<T> =
        object : ParserC<T>() {
            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<T>? = this@ParserC.parse(input, position) ?: other.parse(input, position)
        }
}

object Parsers {
    val whitespace =
        object : ParserC<Unit>() {
            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<Unit>? {
                var pos = position
                while (pos < input.length && input[pos].isWhitespace()) {
                    pos++
                }
                return ParseResult(Unit, pos)
            }
        }

    fun string(s: String): ParserC<String> =
        object : ParserC<String>() {
            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<String>? {
                if (position + s.length > input.length) return null
                val substring = input.substring(position, position + s.length)
                return if (substring == s) ParseResult(s, position + s.length) else null
            }
        }

    fun regex(pattern: String): ParserC<String> =
        object : ParserC<String>() {
            val regex = Regex("^$pattern")

            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<String>? {
                if (position >= input.length) return null
                val match = regex.find(input.substring(position)) ?: return null
                return ParseResult(match.value, position + match.value.length)
            }
        }

    val identifier = regex("[a-zA-Z_$][a-zA-Z0-9_$]*")

    val integer = regex("-?\\d+").map { it.toInt() }

    val stringLiteral =
        object : ParserC<String>() {
            override fun parse(
                input: String,
                position: Int,
            ): ParseResult<String>? {
                if (position >= input.length) return null

                val quote = input[position]
                if (quote != '"' && quote != '\'') return null

                var pos = position + 1
                val sb = StringBuilder()

                while (pos < input.length && input[pos] != quote) {
                    if (input[pos] == '\\' && pos + 1 < input.length) {
                        pos++
                        when (input[pos]) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            else -> sb.append(input[pos])
                        }
                    } else {
                        sb.append(input[pos])
                    }
                    pos++
                }

                return if (pos < input.length && input[pos] == quote) {
                    ParseResult(sb.toString(), pos + 1)
                } else {
                    null
                }
            }
        }

    val boolean = (string("true") or string("false")).map { it.toBoolean() }
}
