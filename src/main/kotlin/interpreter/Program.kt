package interpreter

import interactive.red
import interactive.yellow

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
                    when (stmt.type) {
                        VariableModifier.MUTABLE -> "let"
                        VariableModifier.CONST -> "const"
                    }
                if (stmt.initializer != null) {
                    "$modifier ${stmt.name} = ${formatExpression(stmt.initializer)}"
                } else {
                    "$modifier ${stmt.name}"
                }
            }
            is Statement.ExpressionStmt -> formatExpression(stmt.expr)
            is Statement.FunctionDeclaration -> {
                "function ${stmt.name}(${stmt.args.joinToString(", ")}) { /* ... */ }"
            }
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
            is Expression.FuncExpr -> "function(${expr.args.joinToString(", ")}) { /* ... */ }"
        }

    fun evalLine(lineNumber: Int): Value {
        if (lineNumber < 1 || lineNumber > stmts.size) {
            return Value.StringValue("Line number is out of range.".red())
        }
        val stmt = stmts[lineNumber - 1]
        return stmt.step(env)
    }

    fun assignVariable(
        varName: String,
        expr: Expression,
    ): Value {
        env.assignVariable(varName, expr)
        // if it doesn't throw any exception:
        return Value.StringValue("ok")
    }

    fun invokeFunction(
        funcName: String,
        args: List<String>,
    ): Value = Value.IntValue(4)

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
                    "Commands:\n\tevalLine [lineno]: Evaluates the statement at line number\n\tassign [varname] [varvalue]: Assigns the value to the variable name\n\tinfo: Displays current information about variables\n\thelp: Displays a help message"
                        .yellow(),
                )
        }

    override fun toString(): String {
        val variables = env.getVariables()
        if (variables.isEmpty()) return "No variables defined"

        // Find the longest variable name for alignment
        val maxNameLength = variables.maxOf { it.name.length }
        val maxTypeLength = variables.maxOf { it.type.toString().length }

        // Create headers
        val headerName = "VARIABLE".padEnd(maxNameLength)
        val headerType = "TYPE".padEnd(maxTypeLength)
        val header = "$headerName | $headerType | VALUE"
        val separator = "-".repeat(header.length)

        val sb = StringBuilder()
        sb.appendLine(header)
        sb.appendLine(separator)

        for (v in variables) {
            val name = v.name.padEnd(maxNameLength)
            val type = v.type.toString().padEnd(maxTypeLength)
            val value = env.getVariable(v.name).toString()
            sb.appendLine("$name | $type | $value")
        }

        return sb.toString()
    }
}
