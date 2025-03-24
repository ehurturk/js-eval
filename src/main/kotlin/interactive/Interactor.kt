package interactive

import interpreter.Expression
import interpreter.Program
import interpreter.Request
import interpreter.Value

class Interactor(
    private val program: Program,
) {
    fun startInteractiveSession() {
        while (true) {
            print(">>> ")
            val input = readlnOrNull() ?: break
            try {
                val request = parseCommand(input)
                if (request == null) {
                    println("Invalid command".redBold())
                    continue
                }
                val result = executeCommand(request)
                println("<<< $result")
            } catch (e: Exception) {
                println("<<< ${"Error:".redBold()} ${e.message?.red() ?: e.toString().red()}")
            }
        }
    }

    private fun parseCommand(input: String): Request? {
        val parts = input.trim().split("\\s+".toRegex(), limit = 3)
        return when {
            parts.isEmpty() -> null

            parts[0] == "evalLine" && parts.size >= 2 -> {
                val lineNumber = parts[1].toIntOrNull() ?: return null
                Request.EvalLine(lineNumber)
            }

            parts[0] == "assign" && parts.size >= 3 -> {
                val name = parts[1]
                // TODO: Parse the expression of assign
                //       i.e. when the request is as:
                //       assign a b+3
                //       parse b+3 into Expression.Add
                //                        (
                //                          Expression.VariableReference("b"),
                //                          Expression.Literal(Value.IntValue(3))
                //                        )
                val valueExpr = parseLiteral(parts[2])
                Request.AssignVar(name, valueExpr)
            }

            parts[0] == "invoke" && parts.size >= 2 -> {
                val name = parts[1]
                val args = if (parts.size >= 3) parts[2].split(",").map { it.trim() } else emptyList()
                Request.InvokeFunction(name, args)
            }

            parts[0] == "info" -> Request.PrintInfo
            else -> null
        }
    }

    private fun parseLiteral(value: String): Expression =
        when {
            value.toIntOrNull() != null -> Expression.Literal(Value.IntValue(value.toInt()))
            value == "true" || value == "false" -> Expression.Literal(Value.BoolValue(value.toBoolean()))
            else -> Expression.VariableReference(value)
        }

    private fun executeCommand(request: Request): Value = program.executeRequest(request)
}
