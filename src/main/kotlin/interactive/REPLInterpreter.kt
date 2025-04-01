package interactive

import javafx.application.Application

class REPLInterpreter(
    private val gui: Boolean = true,
) {
    fun start(args: Array<String>) {
        if (gui) {
            Application.launch(InterpreterGUI::class.java, *args)
        } else {
            // TODO: CmdLine filename
        }
    }
}
