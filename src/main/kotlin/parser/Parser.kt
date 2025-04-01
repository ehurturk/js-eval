package parser

import interpreter.Expression
import interpreter.Statement
import interpreter.Symbol
import interpreter.Value
import interpreter.VariableModifier

/*
 * A recursive descent parser approach using Tokens
 */
class Parser(
    private val sourceCode: String,
) {
    private val tokenizer = Tokenizer(sourceCode)

    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (tokenizer.hasMore()) {
            val statement = parseStatement() ?: break
            statements.add(statement)
        }

        if (statements.isEmpty()) return emptyList()
        return statements
    }

    private fun parseStatement(): Statement? {
        tokenizer.skipWhitespace()
        // TODO: refactor this let and const into a single if?
        if (tokenizer.match("function")) {
            return parseFunctionDeclaration()
        }

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
                Symbol.Variable(
                    name,
                    value = null,
                    VariableModifier.MUTABLE,
                    initializer,
                ),
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
                Symbol.Variable(
                    name,
                    value = null,
                    VariableModifier.CONST,
                    initializer,
                ),
            )
        }

        val expr = parseExpression() ?: return null
        tokenizer.match(";")

        return Statement.ExpressionStmt(expr)
    }

    private fun parseFunctionDeclaration(): Statement {
        tokenizer.skipWhitespace()

        val name = tokenizer.identifier() ?: throw ParserException("Expected function name", sourceCode, tokenizer.position)

        tokenizer.skipWhitespace()

        if (!tokenizer.match("(")) throw ParserException("Expected '(' after function name", sourceCode, tokenizer.position)

        val parameters = parseParameterList()

        if (!tokenizer.match(")")) throw ParserException("Expected ')' after parameter list", sourceCode, tokenizer.position)

        tokenizer.skipWhitespace()

        if (!tokenizer.match("{")) throw ParserException("Expected '{' to begin function body", sourceCode, tokenizer.position)

        val body = parseFunctionBody()

        if (!tokenizer.match("}")) throw ParserException("Expected '}' to end function body", sourceCode, tokenizer.position)

        return Statement.FunctionDeclaration(
            Symbol.Function(
                name,
                parameters,
                body,
            ),
        )
    }

    private fun parseParameterList(): List<String> {
        val parameters = mutableListOf<String>()

        tokenizer.skipWhitespace()
        if (tokenizer.peek() == ')') {
            // Empty parameter list
            return parameters
        }

        while (true) {
            tokenizer.skipWhitespace()

            val parameter =
                tokenizer.identifier()
                    ?: throw ParserException("Expected parameter name", sourceCode, tokenizer.position)

            parameters.add(parameter)

            tokenizer.skipWhitespace()
            if (!tokenizer.match(",")) {
                break
            }
        }

        return parameters
    }

    private fun parseFunctionBody(): List<Statement> {
        val statements = mutableListOf<Statement>()
        var foundReturnStmt = false // for now, I will be setting a flag for determining if a return statement exists

        while (tokenizer.hasMore()) {
            tokenizer.skipWhitespace()

            // check return statement
            if (tokenizer.match("return")) {
                foundReturnStmt = true
                tokenizer.skipWhitespace()
                val expression = parseExpression()
                statements.add(Statement.ReturnStmt(expression))
                tokenizer.skipWhitespace()
                tokenizer.match(";")
                tokenizer.skipWhitespace()
            }

            if (tokenizer.peek() == '}') {
                if (!foundReturnStmt) throw ParserException("Expected a return statement in function body", sourceCode, tokenizer.position)
                break
            }

            val statement = parseStatement() ?: throw ParserException("Expected statement in function body", sourceCode, tokenizer.position)
            statements.add(statement)
        }
        return statements
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

    private fun parseFunctionCall(functionName: String): Expression.FunctionCall =
        Expression.FunctionCall(functionName, parseArgumentList())

    // TODO: find ways to combine parseArgumentList with parseParameterList
    private fun parseArgumentList(): List<Expression> {
        val arguments = mutableListOf<Expression>()

        tokenizer.skipWhitespace()

        if (tokenizer.peek() == ')') {
            return arguments
        }

        while (true) {
            tokenizer.skipWhitespace()

            val argument =
                parseExpression()
                    ?: throw ParserException("Expected expression as function argument", sourceCode, tokenizer.position)

            arguments.add(argument)

            tokenizer.skipWhitespace()

            if (!tokenizer.match(",")) {
                break
            }
        }

        return arguments
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
            if (tokenizer.match("(")) {
                // we are calling a function
                val fcExpr = parseFunctionCall(identifier)
                if (!tokenizer.match(")")) throw ParserException("Function call ')' should be closed", sourceCode, tokenizer.position)
                return fcExpr
            }
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

        if (tokenizer.match(")")) throw ParserException("Can't match the opening of ')'", sourceCode, tokenizer.position)

        return null
    }
}
