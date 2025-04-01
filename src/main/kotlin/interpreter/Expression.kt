package interpreter

sealed interface Expression {
    /*
     * Expression type for:
     * a [where a is the variable name]
     */
    data class VariableReference(
        val name: String,
    ) : Expression

    data class Literal(
        val value: Value,
    ) : Expression

    /*
     * Expression type for:
     * a = 10;
     */
    data class Assignment(
        val name: String,
        val value: Expression,
    ) : Expression

    data class FunctionCall(
        val name: String,
        val args: List<Expression>,
    ) : Expression

    data class FuncExpr(
        val args: List<String>,
    ) : Expression

    data class Add(
        val lhs: Expression,
        val rhs: Expression,
    ) : Expression

    data class Sub(
        val lhs: Expression,
        val rhs: Expression,
    ) : Expression

    data class Mul(
        val lhs: Expression,
        val rhs: Expression,
    ) : Expression

    data class Div(
        val lhs: Expression,
        val rhs: Expression,
    ) : Expression
}

fun Expression.eval(env: Environment): Value =
    when (this) {
        is Expression.Assignment -> {
            val exprVal = value.eval(env)
            env.assignVariable(name, value)
            // return the basic stored val without using the dependencies as for the first time
            // the basic stored value would be called anyway.
            exprVal
        }
        is Expression.VariableReference -> env.getVariable(name)
        is Expression.Literal -> value

        // Function related evals
        is Expression.FunctionCall -> {
            val func = env.getFunction(name)
            if (args.size !=
                func.args.size
            ) {
                throw UndefinedBehaviourException(
                    "${args.size} arguments passed to function $name that requires ${func.args.size} arguments",
                )
            }

            val innerEnv = Environment()
            args.forEachIndexed { index, expression ->
                val argval = expression.eval(env)
                innerEnv.declareVariable(func.args[index], Expression.Literal(argval), VariableModifier.MUTABLE)
            }

            try {
                for (stmt in func.body) {
                    stmt.step(innerEnv)
                }
                // TODO: Change the void return value from -999 to an Undefined value.
                Value.IntValue(-999)
            } catch (returned: ReturnValueE) {
                returned.value ?: Value.IntValue(-999)
            }
        }
        is Expression.FuncExpr -> TODO()

        //  Operation related evals
        is Expression.Add -> lhs.eval(env).add(rhs.eval(env))
        is Expression.Div -> lhs.eval(env).div(rhs.eval(env))
        is Expression.Mul -> lhs.eval(env).mul(rhs.eval(env))
        is Expression.Sub -> lhs.eval(env).sub(rhs.eval(env))
    }
