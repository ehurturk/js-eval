package interactive

object ConsoleColors {
    const val RESET = "\u001B[0m"

    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    const val BLACK_BOLD = "\u001B[1;30m"
    const val RED_BOLD = "\u001B[1;31m"
    const val GREEN_BOLD = "\u001B[1;32m"
    const val YELLOW_BOLD = "\u001B[1;33m"
    const val BLUE_BOLD = "\u001B[1;34m"
    const val PURPLE_BOLD = "\u001B[1;35m"
    const val CYAN_BOLD = "\u001B[1;36m"
    const val WHITE_BOLD = "\u001B[1;37m"
}

fun String.red() = "${ConsoleColors.RED}$this${ConsoleColors.RESET}"

fun String.redBold() = "${ConsoleColors.RED_BOLD}$this${ConsoleColors.RESET}"

fun String.green() = "${ConsoleColors.GREEN}$this${ConsoleColors.RESET}"

fun String.greenBold() = "${ConsoleColors.GREEN_BOLD}$this${ConsoleColors.RESET}"

fun String.yellow() = "${ConsoleColors.YELLOW}$this${ConsoleColors.RESET}"

fun String.yellowBold() = "${ConsoleColors.YELLOW_BOLD}$this${ConsoleColors.RESET}"

fun String.blue() = "${ConsoleColors.BLUE}$this${ConsoleColors.RESET}"

fun String.blueBold() = "${ConsoleColors.BLUE_BOLD}$this${ConsoleColors.RESET}"

fun String.purple() = "${ConsoleColors.PURPLE}$this${ConsoleColors.RESET}"

fun String.purpleBold() = "${ConsoleColors.PURPLE_BOLD}$this${ConsoleColors.RESET}"

fun String.cyan() = "${ConsoleColors.CYAN}$this${ConsoleColors.RESET}"

fun String.cyanBold() = "${ConsoleColors.CYAN_BOLD}$this${ConsoleColors.RESET}"
