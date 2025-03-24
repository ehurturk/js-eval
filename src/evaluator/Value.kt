package evaluator

sealed interface Value {
    data class IntValue(
        val value: Int,
    ) : Value

    data class StringValue(
        val value: String,
    ) : Value

    data class BoolValue(
        val value: Boolean,
    ) : Value

    fun asInt(): Int =
        when (this) {
            is IntValue -> value
            else -> throw UndefinedBehaviourException("Expected Int, got $this")
        }

    fun asString(): String =
        when (this) {
            is StringValue -> value
            is IntValue -> value.toString()
            else -> throw UndefinedBehaviourException("Cannot convert $this to String")
        }

    fun asBool(): Boolean =
        when (this) {
            is BoolValue -> value
            else -> throw UndefinedBehaviourException("Expected Boolean, got $this")
        }
}

private sealed interface Op {
    // possibly null right value for unary operations such as increment, decrement, factorial, etc.
    fun apply(
        left: Value,
        right: Value?,
    ): Value

    // right is definitely not null
    data object Add : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    throw UndefinedBehaviourException("Cannot add Boolean values")

                left is Value.StringValue || right is Value.StringValue ->
                    Value.StringValue(left.asString() + right!!.asString())

                left is Value.IntValue && right is Value.IntValue ->
                    Value.IntValue(left.value + right.value)

                else -> throw UndefinedBehaviourException("Unsupported types for addition: $left and $right")
            }
    }

    data object Multiply : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    throw UndefinedBehaviourException("Cannot multiply Boolean values")

                left is Value.IntValue && right is Value.IntValue ->
                    Value.IntValue(left.value * right.value)

                left is Value.StringValue && right is Value.IntValue ->
                    Value.StringValue(left.value.repeat(right.value))

                else -> throw UndefinedBehaviourException("Unsupported types for multiplication: $left and $right")
            }
    }

    data object Subtract : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.IntValue && right is Value.IntValue ->
                    Value.IntValue(left.value - right.value)

                else -> throw UndefinedBehaviourException("Subtraction only supported for integers")
            }
    }

    data object Divide : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.IntValue && right is Value.IntValue -> {
                    if (right.value == 0) throw UndefinedBehaviourException("Division by zero")
                    Value.IntValue(left.value / right.value)
                }

                else -> throw UndefinedBehaviourException("Division only supported for integers")
            }
    }
}

fun Value.add(other: Value): Value = Op.Add.apply(this, other)

fun Value.sub(other: Value): Value = Op.Subtract.apply(this, other)

fun Value.mul(other: Value): Value = Op.Multiply.apply(this, other)

fun Value.div(other: Value): Value = Op.Divide.apply(this, other)
