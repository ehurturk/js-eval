package interpreter

// Todo: value type specific defaults?
private const val DEFAULT_VAL = 0

class Environment {
    // I will use a variable-expression map to mimic the reactive programming paradigm,
    // reevaluating the expressions recursively when needed.
    // e.g:
    // let a = 4;
    // let b = 3;
    // let d = a + 2;
    private val variableExpressionMap: MutableMap<Variable, Expression?> = mutableMapOf()

    // for first declarations
    fun declareVariable(
        name: String,
        initializer: Expression?,
        type: VariableModifier,
    ) {
        val variable = findVariable(name)
        if (variable != null) {
            throw IllegalArgumentException("Variable $name is already declared.")
        }

        val value = initializer?.eval(this) ?: Value.IntValue(DEFAULT_VAL)
        variableExpressionMap[Variable(name, value, type)] = initializer
    }

    fun declareFunction(
        name: String,
        args: List<Expression>,
        body: List<Statement>,
    ) {
    }

    private fun findVariable(name: String): Variable? {
        var variable: Variable? = null
        variableExpressionMap.forEach {
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
        val newVariable = Variable(name, value, variable.type)
        variableExpressionMap.remove(variable)
        variableExpressionMap[newVariable] = expr
    }

    fun getVariable(name: String): Value {
        val variable: Variable = findVariable(name) ?: throw IllegalArgumentException("Variable $name isn't defined")
        // return the reevaluated expression if it has an associated expression, otherwise return its normal value.
        return variableExpressionMap[variable]?.eval(this) ?: variable.value
    }

    fun getVariables(): List<Variable> = variableExpressionMap.keys.toList()
}
