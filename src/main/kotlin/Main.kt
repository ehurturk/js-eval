import interactive.Interactor
import interpreter.Program
import interpreter.Statement
import parser.Parser
import parser.ParserException

// Usage java main [filename]

fun main(args: Array<String>) {
    val source =
        """
        const a = 14;
        let b = 15;
        
        b = b + a;   
        """.trimIndent()

    val parser = Parser(source)

    var parsed: Statement? = null
    try {
        parsed = parser.parse()
    } catch (e: ParserException) {
        println(e.message)
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
