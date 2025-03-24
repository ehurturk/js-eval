import hurturk.emir.evaluator.*

// Usage java main [filename]
fun main(args: Array<String>) {
//    if (args.size != 2) {
//        println("Usage:")
//        println("main [filename]")
//        return
//    }
    // let a = 1;
    // const b = 2;
    // let c = 3;
    // let d = a + b;
    // c + d
    val program: MutableList<Statement> = mutableListOf()
    program.add(
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "a",
            Expression.Literal(Value.IntValue(1)),
        ),
    )
    program.add(
        Statement.Declaration(
            VariableModifier.CONST,
            "b",
            Expression.Literal(Value.IntValue(2)),
        ),
    )
    program.add(
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "c",
            Expression.Literal(Value.IntValue(3)),
        ),
    )
    program.add(
        Statement.Declaration(
            VariableModifier.MUTABLE,
            "d",
            Expression.Add(
                Expression.VariableReference("a"),
                Expression.VariableReference("b"),
            ),
        ),
    )
    program.add(
        Statement.ExpressionStmt(
            Expression.Add(
                Expression.VariableReference("c"),
                Expression.VariableReference("d"),
            ),
        ),
    )
    program.add(
        Statement.ExpressionStmt(
            Expression.Assignment(
                "a",
                Expression.Literal(Value.IntValue(2)),
            ),
        ),
    )
    program.add(
        Statement.ExpressionStmt(
            Expression.Add(
                Expression.VariableReference("c"),
                Expression.VariableReference("d"),
            ),
        ),
    )

    val env = Environment()

    program.forEach {
        it.step(env)
    }
}
