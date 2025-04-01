package interpreter

import parser.Parser

sealed interface Request {
    data class EvalLine(
        val lineNumber: Int,
    ) : Request

    data class AssignVar(
        val name: String,
        val value: Expression,
    ) : Request

    data class InvokeFunction(
        val name: String,
        val args: List<String>,
    ) : Request

    data object PrintInfo : Request

    data object PrintHelp : Request
}

class Program(
    private val stmts: List<Statement>,
) {
    val env = Environment()

    // For executing the file
    fun execute() {
        stmts.forEach {
            it.step(env)
        }
    }

    private fun formatStatement(stmt: Statement): String =
        when (stmt) {
            is Statement.Declaration -> {
                val modifier =
                    when (stmt.variable.type) {
                        VariableModifier.MUTABLE -> "let"
                        VariableModifier.CONST -> "const"
                    }
                if (stmt.variable.expression != null) {
                    "$modifier ${stmt.variable.name} = ${formatExpression(stmt.variable.expression)}"
                } else {
                    "$modifier ${stmt.variable.name}"
                }
            }
            is Statement.ExpressionStmt -> formatExpression(stmt.expr)
            is Statement.FunctionDeclaration -> {
                "function ${stmt.function.name}(${stmt.function.args.joinToString(", ")}) { /* ... */ }"
            }

            is Statement.ReturnStmt -> TODO()
        }

    private fun formatExpression(expr: Expression): String =
        when (expr) {
            is Expression.VariableReference -> expr.name
            is Expression.Literal -> {
                when (val value = expr.value) {
                    is Value.IntValue -> value.value.toString()
                    is Value.StringValue -> "\"${value.value}\""
                    is Value.BoolValue -> value.value.toString()
                }
            }
            is Expression.Assignment -> "${expr.name} = ${formatExpression(expr.value)}"
            is Expression.Add -> "${formatExpression(expr.lhs)} + ${formatExpression(expr.rhs)}"
            is Expression.Sub -> "${formatExpression(expr.lhs)} - ${formatExpression(expr.rhs)}"
            is Expression.Mul -> "${formatExpression(expr.lhs)} * ${formatExpression(expr.rhs)}"
            is Expression.Div -> "${formatExpression(expr.lhs)} / ${formatExpression(expr.rhs)}"
            is Expression.FunctionCall -> "${expr.name}(${expr.args.joinToString(", ")}})"
            is Expression.BAnd -> "${formatExpression(expr.lhs)} && ${formatExpression(expr.rhs)}"
            is Expression.BNeg -> "!${formatExpression(expr.lhs)}"
            is Expression.BOr -> "${formatExpression(expr.lhs)} || ${formatExpression(expr.rhs)}"
            is Expression.Equals -> "${formatExpression(expr.lhs)} == ${formatExpression(expr.rhs)}"
            is Expression.GreaterThan -> "${formatExpression(expr.lhs)} > ${formatExpression(expr.rhs)}"
            is Expression.GreaterThanOrEquals -> "${formatExpression(expr.lhs)} >= ${formatExpression(expr.rhs)}"
            is Expression.LessThan -> "${formatExpression(expr.lhs)} < ${formatExpression(expr.rhs)}"
            is Expression.LessThanOrEquals -> "${formatExpression(expr.lhs)} <= ${formatExpression(expr.rhs)}"
        }

    private fun evalLine(lineNumber: Int): Value {
        if (lineNumber < 1 || lineNumber > stmts.size) {
            return Value.StringValue("Line number is out of range.")
        }
        // TODO: Execute actual line numbers instead of statement elements
        val stmt = stmts[lineNumber - 1]
        return stmt.step(env)
    }

    private fun assignVariable(
        varName: String,
        expr: Expression,
    ): Value {
        env.assignVariable(varName, expr)
        // if it doesn't throw any exception:
        return Value.StringValue("ok")
    }

    private fun invokeFunction(
        funcName: String,
        args: List<String>,
    ): Value {
        val parsedArgs =
            args.map { argString ->
                Parser.parseExpressionFromString(argString)
                    ?: throw IllegalArgumentException("Failed to parse argument: $argString")
            }

        return Expression.FunctionCall(funcName, parsedArgs).eval(env)
    }

    // For interactive requests
    fun executeRequest(req: Request): Value =
        when (req) {
            is Request.EvalLine -> {
                evalLine(req.lineNumber)
            }
            is Request.AssignVar -> {
                assignVariable(req.name, req.value)
            }
            is Request.InvokeFunction -> {
                val result = invokeFunction(req.name, req.args)
                result
            }

            Request.PrintInfo -> Value.StringValue(toString())
            Request.PrintHelp ->
                Value.StringValue(
                    "Commands:\n\tevalLine [lineno]: Evaluates the statement at line number\n\tassign [varname] [varvalue]: Assigns the value to the variable name\n\tinfo: Displays current information about variables\n\thelp: Displays a help message",
                )
        }

    override fun toString(): String {
        val symbols = env.getSymbols()
        if (symbols.isEmpty()) return "No symbols defined"

        // Find the longest variable name for alignment
        val maxNameLength = symbols.maxOf { it.name.length }
        val maxTypeLength = symbols.maxOf { if (it is Symbol.Variable) it.type.toString().length else "FUNCTION".length }

        // Create headers
        val headerName = "SYMBOL".padEnd(maxNameLength)
        val headerType = "TYPE".padEnd(maxTypeLength)
        val header = "$headerName | $headerType | VALUE"
        val separator = "-".repeat(header.length)

        val sb = StringBuilder()
        sb.appendLine(header)
        sb.appendLine(separator)

        for (v in symbols) {
            val name = v.name.padEnd(maxNameLength)
            if (v is Symbol.Variable) {
                val type = v.type.toString().padEnd(maxTypeLength)
                val value = env.getVariable(v.name).toString()
                sb.appendLine("$name | $type | $value")
                continue
            }
            if (v is Symbol.Function) {
                sb.appendLine("$name | FUNCTION | - ")
            }
        }

        return sb.toString()
    }
}
