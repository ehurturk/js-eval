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
        val result = parser.parse()[0] as Statement.Declaration

        assertEquals(VariableModifier.MUTABLE, result.variable.type)
        assertEquals("x", result.variable.name)
        assertTrue(result.variable.expression is Expression.Literal)
        val value = (result.variable.expression as Expression.Literal).value
        assertTrue(value is Value.IntValue)
        assertEquals(5, (value).value)
    }

    @Test
    fun `parser handles variable declarations with const correctly`() {
        val parser = Parser("const y = \"hello\";")
        val result = parser.parse()[0] as Statement.Declaration

        assertEquals(VariableModifier.CONST, result.variable.type)
        assertEquals("y", result.variable.name)
        assertTrue(result.variable.expression is Expression.Literal)
        val value = (result.variable.expression as Expression.Literal).value
        assertTrue(value is Value.StringValue)
        assertEquals("hello", (value).value)
    }

    @Test
    fun `parser handles simple expressions correctly`() {
        val parser = Parser("x + 5;")
        val result = parser.parse()[0] as Statement.ExpressionStmt

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
        val result = parser.parse()[0] as Statement.ExpressionStmt

        assertTrue(result.expr is Expression.Assignment)
        val assignExpr = result.expr as Expression.Assignment
        assertEquals("x", assignExpr.name)
        assertTrue(assignExpr.value is Expression.Literal)
        assertEquals(10, ((assignExpr.value as Expression.Literal).value as Value.IntValue).value)
    }

    @Test
    fun `parser handles complex expressions with precedence correctly`() {
        val parser = Parser("a + b * c - d / e;")
        val result = parser.parse()[0] as Statement.ExpressionStmt

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
        val result = parser.parse()[0] as Statement.ExpressionStmt

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

        val stmts = parser.parse()
        assertNotNull(stmts)
        assertTrue(stmts[0] is Statement.Declaration)
        assertEquals("x", (stmts[0] as Statement.Declaration).variable.name)

        val secondStmt = stmts[1]
        assertNotNull(secondStmt)
        assertTrue(secondStmt is Statement.Declaration)
        assertEquals("y", (secondStmt).variable.name)

        val thirdStmt = stmts[2]
        assertNotNull(thirdStmt)
        assertTrue(thirdStmt is Statement.ExpressionStmt)
        assertTrue((thirdStmt).expr is Expression.Add)
    }

    @Test
    fun `parser handles function declarations correctly`() {
        val parser =
            Parser(
                """
                function add(a, b) {
                    return a + b;
                }
                """.trimIndent(),
            )

        val result = parser.parse()[0] as Statement.FunctionDeclaration

        assertEquals("add", result.function.name)
        assertEquals(2, result.function.args.size)
        assertEquals("a", result.function.args[0])
        assertEquals("b", result.function.args[1])

        assertEquals(1, result.function.body.size)
        assertTrue(result.function.body[0] is Statement.ReturnStmt)

        val returnStmt = result.function.body[0] as Statement.ReturnStmt
        assertNotNull(returnStmt.value)
        assertTrue(returnStmt.value is Expression.Add)

        val addExpr = returnStmt.value as Expression.Add
        assertTrue(addExpr.lhs is Expression.VariableReference)
        assertTrue(addExpr.rhs is Expression.VariableReference)
        assertEquals("a", (addExpr.lhs as Expression.VariableReference).name)
        assertEquals("b", (addExpr.rhs as Expression.VariableReference).name)
    }

    @Test
    fun `parser handles function calls correctly`() {
        val parser = Parser("add(1, 2 + 3);")
        val result = parser.parse()[0] as Statement.ExpressionStmt

        assertTrue(result.expr is Expression.FunctionCall)
        val functionCall = result.expr as Expression.FunctionCall

        assertEquals("add", functionCall.name)
        assertEquals(2, functionCall.args.size)

        // Check first argument is 1
        assertTrue(functionCall.args[0] is Expression.Literal)
        val firstArg = functionCall.args[0] as Expression.Literal
        assertEquals(1, (firstArg.value as Value.IntValue).value)

        // Check second argument is 2 + 3
        assertTrue(functionCall.args[1] is Expression.Add)
        val secondArg = functionCall.args[1] as Expression.Add
        assertTrue(secondArg.lhs is Expression.Literal)
        assertTrue(secondArg.rhs is Expression.Literal)
        assertEquals(2, ((secondArg.lhs as Expression.Literal).value as Value.IntValue).value)
        assertEquals(3, ((secondArg.rhs as Expression.Literal).value as Value.IntValue).value)
    }

    @Test
    fun `parser handles return statements correctly`() {
        val parser = Parser("function test() { return 42; }")
        val result = parser.parse()[0] as Statement.FunctionDeclaration

        assertEquals(1, result.function.body.size)
        assertTrue(result.function.body[0] is Statement.ReturnStmt)

        val returnStmt = result.function.body[0] as Statement.ReturnStmt
        assertNotNull(returnStmt.value)
        assertTrue(returnStmt.value is Expression.Literal)
        assertEquals(42, ((returnStmt.value as Expression.Literal).value as Value.IntValue).value)
    }

    @Test
    fun `parser handles return without value correctly`() {
        val parser = Parser("function test() { return; }")
        val result = parser.parse()[0] as Statement.FunctionDeclaration

        assertEquals(1, result.function.body.size)
        assertTrue(result.function.body[0] is Statement.ReturnStmt)

        val returnStmt = result.function.body[0] as Statement.ReturnStmt
        assertEquals(null, returnStmt.value)
    }

    @Test
    fun `parser handles complex function with multiple statements`() {
        val parser =
            Parser(
                """
                function sum(n) {
                    let total = 0;
                    let i = 1;
                    total = total + i;
                    i = 2;
                    total = total + i;
                    return total;
                }
                """.trimIndent(),
            )

        val result = parser.parse()[0] as Statement.FunctionDeclaration

        assertEquals("sum", result.function.name)
        assertEquals(1, result.function.args.size)
        assertEquals("n", result.function.args[0])

        assertTrue(result.function.body.size >= 3)

        // Check the last statement is a return
        val lastStmt = result.function.body.last()
        assertTrue(lastStmt is Statement.ReturnStmt)

        assertNotNull(lastStmt.value)
        assertTrue(lastStmt.value is Expression.VariableReference)
        assertEquals("total", (lastStmt.value as Expression.VariableReference).name)
    }

    @Test
    fun `parser throws exception for function call with unclosed parenthesis`() {
        val parser = Parser("add(1, 2;")

        assertFailsWith<ParserException> {
            parser.parse()
        }
    }

    @Test
    fun `parser throws exception for function declaration with unclosed body`() {
        val parser = Parser("function test() { return 42;")

        assertFailsWith<ParserException> {
            parser.parse()
        }
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
