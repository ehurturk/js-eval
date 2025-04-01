package parser

import interpreter.Expression
import interpreter.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IntegrationTests {
    @Test
    fun `test complex program parsing`() {
        val source =
            """
            let a = 10;
            let b = 20;
            let sum = a + b;
            let product = a * b;
            sum / product;
            """.trimIndent()

        val parser = Parser(source)
        val program = parser.parse()

        assertNotNull(program)
        assertTrue(program[0] is Statement.Declaration)

        assertEquals(5, program.size)

        // First statement: let a = 10
        assertTrue(program[0] is Statement.Declaration)
        assertEquals("a", (program[0] as Statement.Declaration).variable.name)

        // Last statement: sum / product
        assertTrue(program[4] is Statement.ExpressionStmt)
        assertTrue((program[4] as Statement.ExpressionStmt).expr is Expression.Div)
    }
}
