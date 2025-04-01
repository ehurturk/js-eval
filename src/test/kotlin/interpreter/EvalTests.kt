package interpreter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvalTests {
    /*
     *
     * let a = 1;
     * const b = 2;
     * let c = 3;
     * let d = a + b;
     * c + d;
     *
     * Test:
     *   - a = 1 && b = 2 && c = 3 && d = 3
     *
     * assign a 2
     * Test:
     *   - d = 4
     */
    @Test
    fun `variable updated after assign`() {
        val addCD =
            Statement.ExpressionStmt(
                Expression.Add(
                    Expression.VariableReference("c"),
                    Expression.VariableReference("d"),
                ),
            )
        val initD =
            Statement.Declaration(
                Symbol.Variable(
                    "d",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Add(
                        Expression.VariableReference("a"),
                        Expression.VariableReference("b"),
                    ),
                ),
            )
        val initC =
            Statement.Declaration(
                Symbol.Variable(
                    "c",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(3)),
                ),
            )
        val initB =
            Statement.Declaration(
                Symbol.Variable(
                    "b",
                    null,
                    VariableModifier.CONST,
                    Expression.Literal(Value.IntValue(2)),
                ),
            )
        val initA =
            Statement.Declaration(
                Symbol.Variable(
                    "a",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(1)),
                ),
            )

        val prg = Program(listOf(initA, initB, initC, initD, addCD))
        prg.execute()

        assertEquals(3, prg.env.getVariable("d").asInt())
        assertEquals(1, prg.env.getVariable("a").asInt())
        assertEquals(2, prg.env.getVariable("b").asInt())
        assertEquals(3, prg.env.getVariable("c").asInt())

        prg.executeRequest(Request.AssignVar("a", Expression.Literal(Value.IntValue(2))))

        assertEquals(4, prg.env.getVariable(("d")).asInt())
    }

    /*
     *
     * let a = 1;
     * const b = 2;
     * let c = 3;
     * let d = a + b;
     * b = c + d;
     *
     * Test:
     *   - Exception of reassigning a const variable.
     */
    @Test
    fun `can't change const in initial program`() {
        val changeB =
            Statement.ExpressionStmt(
                Expression.Assignment(
                    "b",
                    Expression.Add(
                        Expression.VariableReference("c"),
                        Expression.VariableReference("d"),
                    ),
                ),
            )
        val initD =
            Statement.Declaration(
                Symbol.Variable(
                    "d",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Add(
                        Expression.VariableReference("a"),
                        Expression.VariableReference("b"),
                    ),
                ),
            )
        val initC =
            Statement.Declaration(
                Symbol.Variable(
                    "c",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(3)),
                ),
            )
        val initB =
            Statement.Declaration(
                Symbol.Variable(
                    "b",
                    null,
                    VariableModifier.CONST,
                    Expression.Literal(Value.IntValue(2)),
                ),
            )
        val initA =
            Statement.Declaration(
                Symbol.Variable(
                    "a",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(1)),
                ),
            )

        val prg = Program(listOf(initA, initB, initC, initD, changeB))

        assertFailsWith<UndefinedBehaviourException> {
            prg.execute()
        }
    }

    /*
     *
     * let a = 1;
     * const b = 2;
     * let c = 3;
     * let d = a + b;
     *
     *
     * Test:
     *   - a = 1 && b = 2 && c = 3 && d = 3
     *
     * assign b 10
     * Test:
     *   - Fails to change a const in a request by outputting "Fail: ..."
     */
    @Test
    fun `can't change const in a request`() {
        val addCD =
            Statement.ExpressionStmt(
                Expression.Add(
                    Expression.VariableReference("c"),
                    Expression.VariableReference("d"),
                ),
            )
        val initD =
            Statement.Declaration(
                Symbol.Variable(
                    "d",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Add(
                        Expression.VariableReference("a"),
                        Expression.VariableReference("b"),
                    ),
                ),
            )
        val initC =
            Statement.Declaration(
                Symbol.Variable(
                    "c",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(3)),
                ),
            )
        val initB =
            Statement.Declaration(
                Symbol.Variable(
                    "b",
                    null,
                    VariableModifier.CONST,
                    Expression.Literal(Value.IntValue(2)),
                ),
            )
        val initA =
            Statement.Declaration(
                Symbol.Variable(
                    "a",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(1)),
                ),
            )

        val prg = Program(listOf(initA, initB, initC, initD, addCD))
        prg.execute()

        assertEquals(3, prg.env.getVariable("d").asInt())
        assertEquals(1, prg.env.getVariable("a").asInt())
        assertEquals(2, prg.env.getVariable("b").asInt())
        assertEquals(3, prg.env.getVariable("c").asInt())

        assertFailsWith<UndefinedBehaviourException> { prg.executeRequest(Request.AssignVar("b", Expression.Literal(Value.IntValue(10)))) }
    }

    @Test
    fun `can't have variables with the same name`() {
        val initC =
            Statement.Declaration(
                Symbol.Variable(
                    "a",
                    null,
                    VariableModifier.CONST,
                    Expression.Literal(Value.IntValue(-1000)),
                ),
            )
        val initB =
            Statement.Declaration(
                Symbol.Variable(
                    "b",
                    null,
                    VariableModifier.CONST,
                    Expression.Literal(Value.IntValue(2)),
                ),
            )
        val initA =
            Statement.Declaration(
                Symbol.Variable(
                    "a",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.Literal(Value.IntValue(1)),
                ),
            )

        val prg = Program(listOf(initA, initB, initC))
        assertFailsWith<IllegalArgumentException> { prg.execute() }
    }

    // New test for function declaration and call
    @Test
    fun `function declaration and call`() {
        // function add(a, b) { return a + b; }
        // let result = add(5, 7);
        // result should be 12

        val functionBody =
            listOf(
                Statement.ReturnStmt(
                    Expression.Add(
                        Expression.VariableReference("a"),
                        Expression.VariableReference("b"),
                    ),
                ),
            )

        val addFunction =
            Statement.FunctionDeclaration(
                Symbol.Function(
                    "add",
                    listOf("a", "b"),
                    functionBody,
                ),
            )

        val resultDecl =
            Statement.Declaration(
                Symbol.Variable(
                    "result",
                    null,
                    VariableModifier.MUTABLE,
                    Expression.FunctionCall(
                        "add",
                        listOf(
                            Expression.Literal(Value.IntValue(5)),
                            Expression.Literal(Value.IntValue(7)),
                        ),
                    ),
                ),
            )

        val prg = Program(listOf(addFunction, resultDecl))
        prg.execute()

        assertEquals(12, prg.env.getVariable("result").asInt())
    }
}
