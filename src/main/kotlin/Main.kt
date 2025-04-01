import interactive.REPLInterpreter

private const val USE_GUI = true

fun main(args: Array<String>) = REPLInterpreter(gui = USE_GUI).start(args)
