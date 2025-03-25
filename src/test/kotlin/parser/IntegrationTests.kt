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
        assertTrue(program is Statement.Declaration)

        var currentStmt: Statement? = program
        val statements = mutableListOf<Statement>()

        while (currentStmt != null) {
            statements.add(currentStmt)
            currentStmt = currentStmt.next
        }

        assertEquals(5, statements.size)

        // First statement: let a = 10
        assertTrue(statements[0] is Statement.Declaration)
        assertEquals("a", (statements[0] as Statement.Declaration).name)

        // Last statement: sum / product
        assertTrue(statements[4] is Statement.ExpressionStmt)
        assertTrue((statements[4] as Statement.ExpressionStmt).expr is Expression.Div)
    }
}
