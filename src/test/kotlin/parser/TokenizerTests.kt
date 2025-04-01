package parser

import kotlin.test.*

class TokenizerTests {
    @Test
    fun `tokenizer identifies JavaScript identifiers correctly`() {
        val tokenizer = Tokenizer("validId _startWithUnderscore $123Dollar")
        assertEquals("validId", tokenizer.identifier())
        tokenizer.skipWhitespace()
        assertEquals("_startWithUnderscore", tokenizer.identifier())
        tokenizer.skipWhitespace()
        assertEquals("$123Dollar", tokenizer.identifier())
    }

    @Test
    fun `tokenizer handles numbers correctly`() {
        val tokenizer = Tokenizer("123 -456 789")
        assertEquals(123, tokenizer.number())
        tokenizer.skipWhitespace()
        assertEquals(-456, tokenizer.number())
        tokenizer.skipWhitespace()
        assertEquals(789, tokenizer.number())
    }

    @Test
    fun `tokenizer handles strings correctly`() {
        val tokenizer = Tokenizer("'single quotes' \"double quotes\"")
        assertEquals("single quotes", tokenizer.string())
        tokenizer.skipWhitespace()
        assertEquals("double quotes", tokenizer.string())
    }

    @Test
    fun `tokenizer handles booleans correctly`() {
        val tokenizer = Tokenizer("true false truthy")
        assertEquals(true, tokenizer.boolean())
        tokenizer.skipWhitespace()
        assertEquals(false, tokenizer.boolean())
        tokenizer.skipWhitespace()
        assertNull(tokenizer.boolean()) // "truthy" is not a boolean literal
    }

    @Test
    fun `tokenizer match function works correctly`() {
        val tokenizer = Tokenizer("let const = +")
        assertTrue(tokenizer.match("let"))
        tokenizer.skipWhitespace()
        assertTrue(tokenizer.match("const"))
        tokenizer.skipWhitespace()
        assertTrue(tokenizer.match("="))
        tokenizer.skipWhitespace()
        assertTrue(tokenizer.match("+"))
        assertFalse(tokenizer.match("anything")) // at end of input
    }

    @Test
    fun `tokenizer peek function returns correct character`() {
        val tokenizer = Tokenizer("abc")
        assertEquals('a', tokenizer.peek())
        tokenizer.position = 3 // end of input
        assertNull(tokenizer.peek())
    }

    @Test
    fun `tokenizer hasMore function works correctly`() {
        val tokenizer = Tokenizer("x")
        assertTrue(tokenizer.hasMore())
        tokenizer.position = 1
        assertFalse(tokenizer.hasMore())
    }
}
