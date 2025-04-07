import interactive.REPLInterpreter

// usage: jseval [-gui] [-help]
fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: jseval [-gui | -help]")
        return
    }
    val argsMap = args.toList()
    val useGui = argsMap.contains("-gui")
    val printHelp = argsMap.contains("-help")

    if (printHelp) {
        println("Usage: jseval [-gui | -help]")
        println("\t-gui: Enables GUI mode for jseval")
        println("\t-help: Displays this message")
        return
    }

    REPLInterpreter(gui = useGui).start(args)
}
