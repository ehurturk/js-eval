package interpreter

// Represent returned values using exception as it will simply exit the function
class ReturnValueE(
    val value: Value?, // nullable because void functions exist
) : RuntimeException()
