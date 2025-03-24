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
                VariableModifier.MUTABLE,
                "d",
                Expression.Add(
                    Expression.VariableReference("a"),
                    Expression.VariableReference("b"),
                ),
                next = addCD,
            )
        val initC =
            Statement.Declaration(
                VariableModifier.MUTABLE,
                "c",
                Expression.Literal(Value.IntValue(3)),
                next = initD,
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
                VariableModifier.MUTABLE,
                "d",
                Expression.Add(
                    Expression.VariableReference("a"),
                    Expression.VariableReference("b"),
                ),
                next = changeB,
            )
        val initC =
            Statement.Declaration(
                VariableModifier.MUTABLE,
                "c",
                Expression.Literal(Value.IntValue(3)),
                next = initD,
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
                VariableModifier.MUTABLE,
                "d",
                Expression.Add(
                    Expression.VariableReference("a"),
                    Expression.VariableReference("b"),
                ),
                next = addCD,
            )
        val initC =
            Statement.Declaration(
                VariableModifier.MUTABLE,
                "c",
                Expression.Literal(Value.IntValue(3)),
                next = initD,
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
        prg.execute()

        assertEquals(3, prg.env.getVariable("d").asInt())
        assertEquals(1, prg.env.getVariable("a").asInt())
        assertEquals(2, prg.env.getVariable("b").asInt())
        assertEquals(3, prg.env.getVariable("c").asInt())

        val res = prg.executeRequest(Request.AssignVar("b", Expression.Literal(Value.IntValue(10))))
        assertEquals("Fail", res.asString().substring(0, 4))
    }

    @Test
    fun `can't have variables with the same name`() {
        val initC =
            Statement.Declaration(
                VariableModifier.CONST,
                "a",
                Expression.Literal(Value.IntValue(-1000)),
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
        assertFailsWith<IllegalArgumentException> { prg.execute() }
    }
}
