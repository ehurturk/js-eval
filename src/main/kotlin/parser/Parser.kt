package parser

import interpreter.Expression
import interpreter.Statement
import interpreter.Value
import interpreter.VariableModifier

/*
 * A recursive descent parser approach using Tokens
 */
class Parser(
    private val sourceCode: String,
) {
    private val tokenizer = Tokenizer(sourceCode)

    fun parse(): Statement? {
        val statements = mutableListOf<Statement>()

        while (tokenizer.hasMore()) {
            val statement = parseStatement() ?: break
            statements.add(statement)
        }

        if (statements.isEmpty()) return null

        for (i in 0 until statements.size - 1) {
            statements[i].next = statements[i + 1]
        }

        return statements.firstOrNull()
    }

    private fun parseStatement(): Statement? {
        tokenizer.skipWhitespace()
        // TODO: refactor this let and const into a single if?
        if (tokenizer.match("let")) {
            tokenizer.skipWhitespace()
            val name = tokenizer.identifier() ?: return null
            tokenizer.skipWhitespace()

            if (!tokenizer.match("=")) return null
            tokenizer.skipWhitespace()

            val initializer = parseExpression() ?: return null
            tokenizer.match(";")

            if (initializer is Expression.Assignment) throw ParserException("Expected expression after '='", sourceCode, tokenizer.position)

            return Statement.Declaration(
                VariableModifier.MUTABLE,
                name,
                initializer,
            )
        }

        if (tokenizer.match("const")) {
            tokenizer.skipWhitespace()
            val name = tokenizer.identifier() ?: return null
            tokenizer.skipWhitespace()

            if (!tokenizer.match("=")) return null
            tokenizer.skipWhitespace()

            val initializer = parseExpression() ?: return null
            tokenizer.match(";")

            if (initializer is Expression.Assignment) throw ParserException("Expected expression after '='", sourceCode, tokenizer.position)

            return Statement.Declaration(
                VariableModifier.CONST,
                name,
                initializer,
            )
        }

        // If not a declaration, it must be an expression statement
        // TODO: Add function support
        val expr = parseExpression() ?: return null
        tokenizer.match(";")

        return Statement.ExpressionStmt(expr)
    }

    /*
     * Expression precedence hierarchy (from the highest entry point to lowest):
     * 1) parseExpression (top level entry point)
     * 2) parseAssignment (handles assignment expressions such as "c = a + d;" or "let d = a+b;" (the a+b) part
     * 3) parseFunctionExpr (handles function expressions such as "c = (a,b) => {}"
     * 4) parseAdditive (handles + and - operations)
     * 5) parseMultiplicative (handles * and / operations)
     * 6) parsePrimary (handles literals/variables/paranthesized exprs)
     */
    private fun parseExpression(): Expression? = parseAssignment()

    private fun parseAssignment(): Expression? {
        tokenizer.skipWhitespace()
        val namePos = tokenizer.position
        val lhs = tokenizer.identifier()
        // We found an identifier
        if (lhs != null) {
            tokenizer.skipWhitespace()
            // There is an assignment
            if (tokenizer.match("=")) {
                tokenizer.skipWhitespace()
                if (tokenizer.peek() == '=') {
                    throw ParserException(
                        "Invalid multiple assignment: '=' cannot follow an assignment expression",
                        sourceCode,
                        tokenizer.position,
                    )
                }
                val value = parseExpression() ?: throw ParserException("Expected expression after '='", sourceCode, tokenizer.position)
                if (value is Expression.Assignment) {
                    throw ParserException(
                        "Can't have nested assignments inside assignment expressions",
                        sourceCode,
                        tokenizer.position,
                    )
                }
                tokenizer.skipWhitespace()
                val res = Expression.Assignment(lhs, value)
                return res
            }
            tokenizer.position = namePos
        }
        val rest = parseAdditive()
        tokenizer.skipWhitespace()
        if (tokenizer.peek() == '=') {
            tokenizer.match("=") // consume it
            throw ParserException("Left hand side of assignment must be a valid identifier.", sourceCode, tokenizer.position - 1)
        }
        return rest
    }

    private fun parseFuncExpr(): Expression? {
        // TODO: Parse it actually
        return parseAdditive()
    }

    private fun parseAdditive(): Expression? {
        var left = parseMultiplicative() ?: return null

        while (true) {
            tokenizer.skipWhitespace()
            when {
                tokenizer.match("+") -> {
                    tokenizer.skipWhitespace()
                    val right =
                        parseMultiplicative() ?: throw ParserException(
                            "Expected expression after +",
                            sourceCode,
                            tokenizer.position,
                        )
                    left = Expression.Add(left, right)
                }
                tokenizer.match("-") -> {
                    tokenizer.skipWhitespace()
                    val right =
                        parseMultiplicative() ?: throw ParserException(
                            "Expected expression after -",
                            sourceCode,
                            tokenizer.position,
                        )
                    left = Expression.Sub(left, right)
                }
                else -> return left
            }
        }
    }

    private fun parseMultiplicative(): Expression? {
        var left = parsePrimary() ?: return null

        while (true) {
            tokenizer.skipWhitespace()
            when {
                tokenizer.match("*") -> {
                    tokenizer.skipWhitespace()
                    val right =
                        parsePrimary() ?: throw ParserException(
                            "Expected expression after *",
                            sourceCode,
                            tokenizer.position,
                        )
                    left = Expression.Mul(left, right)
                }
                tokenizer.match("/") -> {
                    tokenizer.skipWhitespace()
                    val right =
                        parsePrimary() ?: throw ParserException(
                            "Expected expression after /",
                            sourceCode,
                            tokenizer.position,
                        )
                    left = Expression.Div(left, right)
                }
                else -> return left
            }
        }
    }

    private fun parsePrimary(): Expression? {
        tokenizer.skipWhitespace()

        // Number literal
        val number = tokenizer.number()
        if (number != null) {
            return Expression.Literal(Value.IntValue(number))
        }

        // String literal
        val string = tokenizer.string()
        if (string != null) {
            return Expression.Literal(Value.StringValue(string))
        }

        // Boolean literal
        val bool = tokenizer.boolean()
        if (bool != null) {
            return Expression.Literal(Value.BoolValue(bool))
        }

        // Variable reference
        val identifier = tokenizer.identifier()
        if (identifier != null) {
            return Expression.VariableReference(identifier)
        }

        // Parenthesized expression
        if (tokenizer.match("(")) {
            tokenizer.skipWhitespace()
            val expr = parseExpression() ?: throw ParserException("Expected expression after '('", sourceCode, tokenizer.position)
            tokenizer.skipWhitespace()
            if (!tokenizer.match(")")) throw ParserException("Expected ')' after an opening '('", sourceCode, tokenizer.position)
            return expr
        }

        return null
    }
}
