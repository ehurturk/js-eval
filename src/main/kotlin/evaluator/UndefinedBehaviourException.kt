package hurturk.emir.evaluator

class UndefinedBehaviourException(
    private val msg: String,
) : Exception("Undefined behaviour occurred: $msg")
