import interactive.Interactor
import interpreter.Program
import interpreter.Statement
import parser.Parser
import parser.ParserException

// Usage java main [filename]

fun main(args: Array<String>) {
    val source =
        """
        let a = 1;
        const b = 2;
        let c = a + b;
        let d = c + a;
        c + d;
        """.trimIndent()

    val parser = Parser(source)

    var parsed: List<Statement> = emptyList()
    try {
        parsed = parser.parse()
    } catch (e: ParserException) {
        println(e.message)
        println("No file loaded.")
    }

    val prg = Program(parsed)
    val interactive = Interactor(prg)
    try {
        prg.execute()
    } catch (e: Exception) {
        println("Error occurred:\n\t${e.message}")
        println("No file loaded.")
    }
    interactive.startInteractiveSession()
}
