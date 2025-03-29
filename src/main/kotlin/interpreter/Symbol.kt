package interpreter

enum class VariableModifier {
    MUTABLE,
    CONST,
}

sealed interface Symbol {
    val name: String

    data class Variable(
        override val name: String,
        val value: Value?, // TODO: might get rid of value?
        val type: VariableModifier,
        val expression: Expression? = null,
    ) : Symbol

    data class Function(
        override val name: String,
        val args: List<String>,
        val body: List<Statement>,
    ) : Symbol
}
