package parser

import interpreter.Expression
import interpreter.Statement
import interpreter.Value
import interpreter.VariableModifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTests {
    @Test
    fun `parser handles variable declarations with let correctly`() {
        val parser = Parser("let x = 5;")
        val result = parser.parse() as Statement.Declaration

        assertEquals(VariableModifier.MUTABLE, result.type)
        assertEquals("x", result.name)
        assertTrue(result.initializer is Expression.Literal)
        val value = (result.initializer as Expression.Literal).value
        assertTrue(value is Value.IntValue)
        assertEquals(5, (value).value)
    }

    @Test
    fun `parser handles variable declarations with const correctly`() {
        val parser = Parser("const y = \"hello\";")
        val result = parser.parse() as Statement.Declaration

        assertEquals(VariableModifier.CONST, result.type)
        assertEquals("y", result.name)
        assertTrue(result.initializer is Expression.Literal)
        val value = (result.initializer as Expression.Literal).value
        assertTrue(value is Value.StringValue)
        assertEquals("hello", (value).value)
    }

    @Test
    fun `parser handles simple expressions correctly`() {
        val parser = Parser("x + 5;")
        val result = parser.parse() as Statement.ExpressionStmt

        assertTrue(result.expr is Expression.Add)
        val addExpr = result.expr as Expression.Add
        assertTrue(addExpr.lhs is Expression.VariableReference)
        assertEquals("x", (addExpr.lhs as Expression.VariableReference).name)
        assertTrue(addExpr.rhs is Expression.Literal)
        assertEquals(5, ((addExpr.rhs as Expression.Literal).value as Value.IntValue).value)
    }

    @Test
    fun `parser handles assignments correctly`() {
        val parser = Parser("x = 10;")
        val result = parser.parse() as Statement.ExpressionStmt

        assertTrue(result.expr is Expression.Assignment)
        val assignExpr = result.expr as Expression.Assignment
        assertEquals("x", assignExpr.name)
        assertTrue(assignExpr.value is Expression.Literal)
        assertEquals(10, ((assignExpr.value as Expression.Literal).value as Value.IntValue).value)
    }

    @Test
    fun `parser handles complex expressions with precedence correctly`() {
        val parser = Parser("a + b * c - d / e;")
        val result = parser.parse() as Statement.ExpressionStmt

        // Should parse as: (a + (b * c)) - (d / e)
        assertTrue(result.expr is Expression.Sub)
        val subExpr = result.expr as Expression.Sub

        // Left side: a + (b * c)
        assertTrue(subExpr.lhs is Expression.Add)
        val addExpr = subExpr.lhs as Expression.Add
        assertTrue(addExpr.lhs is Expression.VariableReference)
        assertEquals("a", (addExpr.lhs as Expression.VariableReference).name)

        assertTrue(addExpr.rhs is Expression.Mul)
        val mulExpr = addExpr.rhs as Expression.Mul
        assertTrue(mulExpr.lhs is Expression.VariableReference)
        assertEquals("b", (mulExpr.lhs as Expression.VariableReference).name)
        assertTrue(mulExpr.rhs is Expression.VariableReference)
        assertEquals("c", (mulExpr.rhs as Expression.VariableReference).name)

        // Right side: d / e
        assertTrue(subExpr.rhs is Expression.Div)
        val divExpr = subExpr.rhs as Expression.Div
        assertTrue(divExpr.lhs is Expression.VariableReference)
        assertEquals("d", (divExpr.lhs as Expression.VariableReference).name)
        assertTrue(divExpr.rhs is Expression.VariableReference)
        assertEquals("e", (divExpr.rhs as Expression.VariableReference).name)
    }

    @Test
    fun `parser handles parentheses correctly`() {
        val parser = Parser("(a + b) * c;")
        val result = parser.parse() as Statement.ExpressionStmt

        assertTrue(result.expr is Expression.Mul)
        val mulExpr = result.expr as Expression.Mul

        assertTrue(mulExpr.lhs is Expression.Add)
        val addExpr = mulExpr.lhs as Expression.Add
        assertTrue(addExpr.lhs is Expression.VariableReference)
        assertEquals("a", (addExpr.lhs as Expression.VariableReference).name)
        assertTrue(addExpr.rhs is Expression.VariableReference)
        assertEquals("b", (addExpr.rhs as Expression.VariableReference).name)

        assertTrue(mulExpr.rhs is Expression.VariableReference)
        assertEquals("c", (mulExpr.rhs as Expression.VariableReference).name)
    }

    @Test
    fun `parser handles multiple statements correctly`() {
        val parser =
            Parser(
                """
                let x = 10;
                let y = 20;
                x + y;
                """.trimIndent(),
            )

        val firstStmt = parser.parse()
        assertNotNull(firstStmt)
        assertTrue(firstStmt is Statement.Declaration)
        assertEquals("x", (firstStmt).name)

        val secondStmt = firstStmt.next
        assertNotNull(secondStmt)
        assertTrue(secondStmt is Statement.Declaration)
        assertEquals("y", (secondStmt).name)

        val thirdStmt = secondStmt.next
        assertNotNull(thirdStmt)
        assertTrue(thirdStmt is Statement.ExpressionStmt)
        assertTrue((thirdStmt).expr is Expression.Add)
    }

    @Test
    fun `parser throws exception for invalid assignment target`() {
        val parser = Parser("5 = x;")

        assertFailsWith<ParserException> {
            parser.parse()
        }
    }

    @Test
    fun `parser throws exception for chained assignments`() {
        val parser = Parser("x = y = 5;")

        assertFailsWith<ParserException> {
            parser.parse()
        }
    }

    @Test
    fun `parser throws exception for missing closing parenthesis`() {
        val parser = Parser("(x + y;")

        assertFailsWith<ParserException> {
            parser.parse()
        }
    }

    @Test
    fun `parser throws exception for missing expression after operator`() {
        val parser = Parser("x + ;")

        assertFailsWith<ParserException> {
            parser.parse()
        }
    }
}
