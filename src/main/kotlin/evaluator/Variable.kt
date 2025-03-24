package hurturk.emir.evaluator

enum class VariableModifier {
    MUTABLE,
    CONST,
}

data class Variable(
    val name: String,
    val value: Value,
    val type: VariableModifier,
)
