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
 *
 */

sealed interface Statement {
    data class Declaration(
        val variable: Symbol.Variable,
    ) : Statement

    data class ExpressionStmt(
        val expr: Expression,
    ) : Statement

    data class FunctionDeclaration(
        val function: Symbol.Function,
    ) : Statement

    data class ReturnStmt(
        val value: Expression?,
    ) : Statement
}

// Executes the current statement
fun Statement.step(env: Environment): Value {
    when (this) {
        is Statement.Declaration -> {
            env.declareVariable(variable.name, variable.expression, variable.type)
            return Value.StringValue("Declared variable ${variable.name}")
        }

        is Statement.ExpressionStmt -> {
            return expr.eval(env)
        }

        is Statement.FunctionDeclaration -> {
            env.declareFunction(function.name, function.args, function.body)
            return Value.StringValue("Declared function ${function.name}")
        }

        is Statement.ReturnStmt -> {
            val returnVal = value?.eval(env)
            throw ReturnValueE(returnVal)
        }
    }
}
