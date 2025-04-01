package interactive

import interpreter.Program
import interpreter.Statement
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import parser.Parser
import parser.ParserException
import java.io.File

class REPLInterpreter {
    fun start(args: Array<String>) {
        Application.launch(InterpreterGUI::class.java, *args)
    }
}

class InterpreterGUI : Application() {
    private var program: Program? = null
    private var interactor: Interactor? = null

    private lateinit var filePathField: TextField
    private lateinit var consoleOutput: TextArea
    private lateinit var commandInput: TextField

    override fun start(stage: Stage) {
        stage.title = "JavaScript Interpreter"

        val topSection = createTopSection()

        consoleOutput =
            TextArea().apply {
                isEditable = false
                style = "-fx-font-family: monospace;"
            }
        val centerSection =
            VBox(10.0, Label("Console Output"), consoleOutput).apply {
                padding = Insets(10.0)
                VBox.setVgrow(consoleOutput, Priority.ALWAYS)
            }

        val bottomSection = createBottomSection()

        // Main layout
        val root =
            BorderPane().apply {
                top = topSection
                center = centerSection
                bottom = bottomSection
            }

        val scene = Scene(root, 800.0, 600.0)
        stage.scene = scene
        stage.show()
    }

    private fun createTopSection(): VBox {
        filePathField = TextField()

        val loadButton =
            Button("Load").apply {
                setOnAction {
                    loadFile(filePathField.text)
                }
            }

        val browseButton =
            Button("Browse").apply {
                setOnAction {
                    val fileChooser =
                        FileChooser().apply {
                            title = "Select JavaScript File"
                            extensionFilters.add(FileChooser.ExtensionFilter("JavaScript Files", "*.js"))
                        }
                    val file = fileChooser.showOpenDialog(scene.window)
                    if (file != null) {
                        filePathField.text = file.absolutePath
                    }
                }
            }

        val fileControls =
            HBox(10.0, Label("File Path:"), filePathField, loadButton, browseButton).apply {
                HBox.setHgrow(filePathField, Priority.ALWAYS)
            }

        return VBox(10.0, fileControls).apply {
            padding = Insets(10.0)
        }
    }

    private fun createBottomSection(): VBox {
        commandInput =
            TextField().apply {
                setOnKeyPressed { event ->
                    if (event.code == KeyCode.ENTER) {
                        executeCommand()
                    }
                }
            }

        val executeButton =
            Button("Execute").apply {
                setOnAction {
                    executeCommand()
                }
            }

        val inputControls =
            HBox(10.0, commandInput, executeButton).apply {
                HBox.setHgrow(commandInput, Priority.ALWAYS)
            }

        // Quick access buttons
        val evalLineButton =
            Button("eval line").apply {
                prefWidth = 100.0
                setOnAction { commandInput.text = "evalLine " }
            }

        val assignButton =
            Button("assign").apply {
                prefWidth = 100.0
                setOnAction { commandInput.text = "assign " }
            }

        val invokeButton =
            Button("invoke").apply {
                prefWidth = 100.0
                setOnAction { commandInput.text = "invoke " }
            }

        val infoButton =
            Button("info").apply {
                prefWidth = 100.0
                setOnAction { commandInput.text = "info" }
            }

        val helpButton =
            Button("help").apply {
                prefWidth = 100.0
                setOnAction { commandInput.text = "help" }
            }

        val quickAccessButtons = HBox(5.0, evalLineButton, assignButton, invokeButton, infoButton, helpButton)

        return VBox(10.0, Label("Command Input"), inputControls, quickAccessButtons).apply {
            padding = Insets(10.0)
        }
    }

    private fun loadFile(path: String) {
        try {
            val inputStream = File(path).inputStream()
            val src = inputStream.bufferedReader().use { it.readText() }

            val parser = Parser(src)
            val parsed: List<Statement> =
                try {
                    parser.parse()
                } catch (e: ParserException) {
                    appendConsoleOutput("Parser error: ${e.message}")
                    return
                }

            program = Program(parsed)
            interactor = Interactor(program!!)

            try {
                program!!.execute()
                appendConsoleOutput("Loaded and executed file: $path")
            } catch (e: Exception) {
                appendConsoleOutput("Execution error: ${e.message}")
            }
        } catch (e: Exception) {
            appendConsoleOutput("Failed to load file: ${e.message}")
        }
    }

    private fun executeCommand() {
        val command = commandInput.text
        if (command.isBlank()) return

        appendConsoleOutput(">>> $command")

        if (program == null) {
            appendConsoleOutput("<<< No program loaded. Please load a file first.")
            return
        }

        try {
            val result = interactor?.processCommand(command) ?: "Command processing failed"
            appendConsoleOutput("<<< $result")
        } catch (e: Exception) {
            appendConsoleOutput("<<< Error: ${e.message}")
        }

        commandInput.clear()
    }

    private fun appendConsoleOutput(text: String) {
        consoleOutput.appendText("$text\n")
        consoleOutput.scrollTop = Double.MAX_VALUE // Scroll to bottom
    }
}
