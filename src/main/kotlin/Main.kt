import interactive.Interactor
import interpreter.Program
import interpreter.Statement
import parser.Parser
import parser.ParserException

// Usage java main [filename]

fun main(args: Array<String>) {
    val source =
        """
        function double(x) {
          return x * 2;
        }

        const one = 1; 
        let x = one + 2;
        let y = double(4);

        x + y
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
