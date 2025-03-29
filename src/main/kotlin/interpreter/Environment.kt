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
    private val symbolTable: MutableMap<String, Symbol> = mutableMapOf()

    // for first declarations
    fun declareVariable(
        name: String,
        initializer: Expression?,
        type: VariableModifier,
    ) {
        if (symbolTable.containsKey(name)) throw IllegalArgumentException("Symbol $name is already declared")
        val value = initializer?.eval(this) ?: Value.IntValue(DEFAULT_VAL)
        symbolTable[name] = Symbol.Variable(name, value, type, initializer)
    }

    //  TODO: Change params to list of expressions
    fun declareFunction(
        name: String,
        parameters: List<String>,
        body: List<Statement>,
    ) {
        if (symbolTable.containsKey(name)) {
            throw IllegalArgumentException("Symbol $name is already declared.")
        }

        symbolTable[name] = Symbol.Function(name, parameters, body)
    }

    fun assignVariable(
        name: String,
        expr: Expression,
    ) {
        val symbol = symbolTable[name] ?: throw IllegalArgumentException("Symbol $name isn't defined.")

        if (symbol !is Symbol.Variable) {
            throw IllegalArgumentException("$name is not a variable")
        }

        if (symbol.type == VariableModifier.CONST) {
            throw UndefinedBehaviourException("Can't reassign a const variable.")
        }

        val value = expr.eval(this)
        symbolTable[name] = Symbol.Variable(name, value, symbol.type, expr)
    }

    fun getVariable(name: String): Value {
        val symbol = symbolTable[name] ?: throw IllegalArgumentException("Symbol $name is not defined")

        if (symbol !is Symbol.Variable) {
            throw IllegalArgumentException("$name is not a variable")
        }

        // Return the reevaluated expression if available, otherwise the stored value
        return symbol.expression?.eval(this) ?: (symbol.value ?: Value.IntValue(DEFAULT_VAL))
    }

    fun getFunction(name: String): Symbol.Function {
        val symbol = symbolTable[name] ?: throw IllegalArgumentException("Symbol $name is not defined")

        return when (symbol) {
            is Symbol.Function -> symbol
            else -> throw IllegalArgumentException("$name is not a function")
        }
    }

    fun getSymbols(): List<Symbol> = symbolTable.values.toList()

//    fun getVariables(): List<Symbol.Variable> = variableExpressionMap.keys.toList()
}
