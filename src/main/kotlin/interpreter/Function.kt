package interpreter

// Todo: might support having closure (accessing variables from outer environments) if I have sufficient time left
data class Function(
    val name: String,
    val args: List<String>,
    val body: List<Statement>,
)
