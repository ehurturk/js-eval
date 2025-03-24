package interpreter

/*
 * Each JS line can be categorized as 2 statements:
 * 1) Declaration
 * 2) Function Declaration
 * 3) Expression
 *
 * Declaration
 * ============
 * Declaration is when a line contains
 * [VARIABLE_MODIFIER] [VARIABLE_NAME] = [VARIABLE_VALUE_INITIALIZER]
 * Note that the initializer can be null
 *
 * Function Declaration
 * ====================
 * Function declaration is:
 * function [FUNC_NAME]([FUNC_ARGS]) {
 *      [FUNC_BODY]
 * }
 * Note that the function body is a list of Statements.
 *
 * Expression
 * ============
 * A JS expression can be described as
 * a) VariableReference:
 *         - [VARIABLE_NAME] -> returns the variable's Value.
 * b) Assignment (in JavaScript assignment is also an expression):
 *         - [VARIABLE_NAME] = Expression -> updates the variable's Value by computing the expression.
 * c) Arithmetic Operators
 * d) Function Expression (similar to a lambda (anonymous) function):
 *         - const [FUNC_NAME] = function([FUNC_ARGS]) {
 *                 [FUNC_BODY]
 *           }
 *         - const [FUNC_NAME] = ([FUNC_ARGS]) => [FUNC_BODY];
 */

sealed interface Statement {
    var next: Statement?
    val lastInSequence: Statement
        get() {
            if (next == null) return this
            return next!!.lastInSequence
        }

    data class Declaration(
        val type: VariableModifier,
        val name: String,
        val initializer: Expression?,
        override var next: Statement? = null,
    ) : Statement

    data class ExpressionStmt(
        val expr: Expression,
        override var next: Statement? = null,
    ) : Statement

    data class FunctionDeclaration(
        val name: String,
        val args: List<String>,
        val body: List<Statement>,
        override var next: Statement? = null,
    ) : Statement
}

// Executes the current statement and returns the next
fun Statement.step(env: Environment): Statement? =
    when (this) {
        // For simple one-line operations like declaration and expressions, simply returning the next will be enough.
        is Statement.Declaration -> {
            env.declareVariable(name, initializer, type)
            next
        }
        is Statement.ExpressionStmt -> {
            expr.eval(env)
            next
        }
        is Statement.FunctionDeclaration -> TODO()
    }
