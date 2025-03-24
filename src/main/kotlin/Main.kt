import interactive.Interactor
import interpreter.Expression
import interpreter.Program
import interpreter.Statement
import interpreter.Value
import interpreter.VariableModifier

// Usage java main [filename]

fun main(args: Array<String>) {
    val addCD =
        Statement.ExpressionStmt(
            Expression.Add(
                Expression.VariableReference("c"),
                Expression.VariableReference("d"),
            ),
        )
    val initD =
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "d",
            Expression.Add(
                Expression.VariableReference("a"),
                Expression.VariableReference("b"),
            ),
            next = addCD,
        )
    val initBool =
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "ehh",
            Expression.Literal(Value.BoolValue(true)),
            next = initD,
        )
    val initC =
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "c",
            Expression.Literal(Value.IntValue(3)),
            next = initBool,
        )
    val initB =
        Statement.Declaration(
            VariableModifier.CONST,
            "b",
            Expression.Literal(Value.IntValue(2)),
            next = initC,
        )
    val initA =
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "a",
            Expression.Literal(Value.IntValue(1)),
            next = initB,
        )

    val prg = Program(initA)
    val interactive = Interactor(prg)
    try {
        prg.execute()
    } catch (e: Exception) {
        println("Error occurred:\n\t${e.message}")
        println("No file loaded.")
    }
    interactive.startInteractiveSession()
}
