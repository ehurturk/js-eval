package parser

sealed class Token {
    abstract val type: TokenType
    abstract val value: String

    data class IntToken(
        override val value: String,
    ) : Token() {
        override val type = TokenType.INT
    }

    data class StringToken(
        override val value: String,
    ) : Token() {
        override val type = TokenType.STRING
    }

    data class BoolToken(
        override val value: String,
    ) : Token() {
        override val type = TokenType.BOOL
    }

    data class IdentifierToken(
        override val value: String,
    ) : Token() {
        override val type = TokenType.IDENTIFIER
    }

    data class SymbolToken(
        override val type: TokenType,
    ) : Token() {
        override val value: String = type.toString()
    }
}

enum class TokenType {
    INT,
    STRING,
    BOOL,
    IDENTIFIER,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    LEFT_PAREN,
    RIGHT_PAREN,
    EQUALS,
    SEMICOLON,
    LET,
    CONST,
}

class Tokenizer(
    private val input: String,
) {
    var position = 0

    fun hasMore(): Boolean = position < input.length

    fun skipWhitespace() {
        while (position < input.length && input[position].isWhitespace()) {
            position++
        }
    }

    fun match(s: String): Boolean {
        if (position + s.length > input.length) return false
        if (input.substring(position, position + s.length) != s) return false
        position += s.length
        return true
    }

    fun peek(): Char? = if (position < input.length) input[position] else null

    fun identifier(): String? {
        if (position >= input.length) return null
        if (!input[position].isJavaScriptIdentifierStart()) return null

        val start = position
        position++

        while (position < input.length && input[position].isJavaScriptIdentifierPart()) {
            position++
        }

        return input.substring(start, position)
    }

    fun number(): Int? {
        if (position >= input.length) return null

        val isNegative = input[position] == '-'
        if (isNegative) position++

        if (position >= input.length || !input[position].isDigit()) {
            // restore the pos if the token was - (minus without a digit)
            if (isNegative) position--
            return null
        }

        val start = position
        while (position < input.length && input[position].isDigit()) {
            position++
        }

        val numStr = input.substring(if (isNegative) start - 1 else start, position)
        return numStr.toIntOrNull()
    }

    fun string(): String? {
        if (position >= input.length) return null

        val quote = input[position]
        if (quote != '"' && quote != '\'') return null

        position++
        val start = position

        while (position < input.length && input[position] != quote) {
            if (input[position] == '\\' && position + 1 < input.length) {
                position += 2
            } else {
                position++
            }
        }

        if (position >= input.length) return null

        val result = input.substring(start, position)
        position++
        return result
    }

    fun boolean(): Boolean? {
        val trueStr = "true"
        val falseStr = "false"
        if (position + trueStr.length <= input.length && input.substring(position, position + 4) == trueStr) {
            position += trueStr.length
            return true
        }

        if (position + falseStr.length <= input.length && input.substring(position, position + 5) == falseStr) {
            position += falseStr.length
            return false
        }

        return null
    }
}

/*
 * Identifiers can only be either:
 * varname
 * _varname
 * $varname
 */
private fun Char.isJavaScriptIdentifierStart(): Boolean = isLetter() || this == '_' || this == '$'

/*
 * We can have digits as a part of a variable identifier
 */
private fun Char.isJavaScriptIdentifierPart(): Boolean = isJavaScriptIdentifierStart() || isDigit()
