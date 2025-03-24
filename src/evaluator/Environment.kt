package evaluator

// Todo: value type specific defaults?
private const val DEFAULT_VAL = 0

class Environment {
    // I will use a directed dependency graph to update the variables to mimic the reactive programming paradigm.
    // map keys: variables
    //     values: any variables that are the dependencies of the current variable.
    // e.g:
    // let a = 4;
    // let b = 3;
    // let d = a + 2;
    // Then the graph would be as:
    // Variable("a", 4, MUTABLE) at 0xfef -> emptyList()
    // Variable("b", 3, MUTABLE) at 0xff7 -> emptyList()
    // Variable("d", 6, MUTABLE) at 0xfff -> listOf(0xff7)
    // TODO: Avoid circular dependencies
    private val dependencyGraph: MutableMap<Variable, MutableList<Variable>> = mutableMapOf()

    // TODO: might get rid of this
    private val variableExpressionMap: MutableMap<String, Expression> = mutableMapOf()

    // for first declarations
    fun declareVariable(
        name: String,
        initializer: Expression?,
        type: VariableModifier,
    ) {
        val value = initializer?.eval(this) ?: Value.IntValue(DEFAULT_VAL)
        dependencyGraph[Variable(name, value, type)] = mutableListOf()

        if (initializer != null) {
            variableExpressionMap[name] = initializer
            // Gather the dependencies from the initializer
            addDependenciesFromExpression(name, initializer)
        }
    }

    private fun addDependenciesFromExpression(
        name: String,
        expr: Expression,
    ) {
        when (expr) {
            is Expression.VariableReference -> {
                val variable = findVariable(name) ?: return
                // if the dependent variable isn't in the environment then return
                // todo: error?
                val dep = findVariable(expr.name) ?: return
                dependencyGraph[variable]?.add(dep)
            }

            // if the expression is literal we just return without adding any edge to the dependency graph
            is Expression.Literal -> return

            is Expression.Add -> {
                addDependenciesFromExpression(name, expr.lhs)
                addDependenciesFromExpression(name, expr.rhs)
            }
            is Expression.Sub -> {
                addDependenciesFromExpression(name, expr.lhs)
                addDependenciesFromExpression(name, expr.rhs)
            }
            is Expression.Mul -> {
                addDependenciesFromExpression(name, expr.lhs)
                addDependenciesFromExpression(name, expr.rhs)
            }
            is Expression.Div -> {
                addDependenciesFromExpression(name, expr.lhs)
                addDependenciesFromExpression(name, expr.rhs)
            }

            is Expression.Assignment -> {}
            is Expression.FuncExpr -> {}
        }
    }

    private fun findVariable(name: String): Variable? {
        var variable: Variable? = null
        dependencyGraph.forEach {
            if (it.key.name == name) variable = it.key
        }
        return variable
    }

    fun assignVariable(
        name: String,
        expr: Expression,
    ) {
        val value = expr.eval(this)
        val variable: Variable = findVariable(name) ?: throw IllegalArgumentException("Variable $name isn't defined.")
        if (variable.type == VariableModifier.CONST) {
            throw UndefinedBehaviourException("Can't reassign a const variable.")
        }
        variableExpressionMap[name] = expr
        val newVariable = Variable(name, value, variable.type)

        dependencyGraph.remove(variable)
        dependencyGraph[newVariable] = mutableListOf()
        addDependenciesFromExpression(name, expr)
    }

    fun getVariable(name: String): Value {
        var variable: Variable? = null
        dependencyGraph.forEach {
            if (it.key.name == name) variable = it.key
        }
        if (variable == null) throw IllegalArgumentException("Variable $name isn't defined")
        // return the reevaluated expression if it has an associated expression, otherwise return its normal value.
        return variableExpressionMap[variable!!.name]?.eval(this)
            ?: variable!!.value
    }
}
