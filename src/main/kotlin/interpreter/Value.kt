package interpreter

// TODO: Implement boolean operations for BoolValue
//       Implement String operations for StringValue
sealed interface Value {
    data class IntValue(
        val value: Int,
    ) : Value {
        override fun toString(): String = "$value"
    }

    data class StringValue(
        val value: String,
    ) : Value {
        override fun toString(): String = value
    }

    data class BoolValue(
        val value: Boolean,
    ) : Value {
        override fun toString(): String = "$value"
    }

    fun asInt(): Int =
        when (this) {
            is IntValue -> value
            is BoolValue -> {
                if (value) {
                    1
                } else {
                    0
                }
            }
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
            is IntValue -> value > 0
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
                    // NOTE: I am going to do as the JS way and treat booleans as integers so:
                    // 10 + true && false will output -> false
                    // 10 + false && true will output -> true
                    Value.IntValue(left.asInt() + right!!.asInt())

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
                    Value.IntValue(left.asInt() * right!!.asInt())

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
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.IntValue(left.asInt() - right!!.asInt())

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
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.IntValue(left.asInt() / right!!.asInt())

                left is Value.IntValue && right is Value.IntValue -> {
                    if (right.value == 0) throw UndefinedBehaviourException("Division by zero")
                    Value.IntValue(left.value / right.value)
                }

                else -> throw UndefinedBehaviourException("Division only supported for integers")
            }
    }

    data object And : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.IntValue || right is Value.IntValue ->
                    Value.BoolValue(left.asBool() && right!!.asBool())

                left is Value.BoolValue && right is Value.BoolValue -> {
                    Value.BoolValue(left.value && right.value)
                }
                else -> throw UndefinedBehaviourException("Logical && only supported for boolean values.")
            }
    }

    data object Or : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.IntValue || right is Value.IntValue ->
                    Value.BoolValue(left.asBool() || right!!.asBool())
                left is Value.BoolValue && right is Value.BoolValue -> {
                    Value.BoolValue(left.value || right.value)
                }
                else -> throw UndefinedBehaviourException("Logical || only supported for boolean values.")
            }
    }

    data object Neg : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when (left) {
                is Value.IntValue -> Value.BoolValue(!left.asBool())
                is Value.BoolValue -> {
                    Value.BoolValue(!left.value)
                }

                else -> throw UndefinedBehaviourException("Logical negation only supported for boolean values.")
            }
    }

    data object LessThan : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.BoolValue(left.asInt() < right!!.asInt())
                left is Value.IntValue && right is Value.IntValue -> {
                    Value.BoolValue(left.value < right.value)
                }
                else -> throw UndefinedBehaviourException("Boolean less than comparison only supported for integer values.")
            }
    }

    data object GreaterThan : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.BoolValue(left.asInt() > right!!.asInt())
                left is Value.IntValue && right is Value.IntValue -> {
                    Value.BoolValue(left.value > right.value)
                }
                else -> throw UndefinedBehaviourException("Boolean greater than comparison only supported for integer values.")
            }
    }

    data object GreaterThanOrEq : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.BoolValue(left.asInt() >= right!!.asInt())
                left is Value.IntValue && right is Value.IntValue -> {
                    Value.BoolValue(left.value >= right.value)
                }
                else -> throw UndefinedBehaviourException("Boolean greater than or equal comparison only supported for integer values.")
            }
    }

    data object LessThanOrEq : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.BoolValue(left.asInt() <= right!!.asInt())
                left is Value.IntValue && right is Value.IntValue -> {
                    Value.BoolValue(left.value <= right.value)
                }
                else -> throw UndefinedBehaviourException("Boolean less than or equal comparison only supported for integer values.")
            }
    }

    data object Equals : Op {
        override fun apply(
            left: Value,
            right: Value?,
        ): Value =
            when {
                left is Value.BoolValue || right is Value.BoolValue ->
                    Value.BoolValue(left.asInt() == right!!.asInt())
                left is Value.IntValue && right is Value.IntValue -> {
                    Value.BoolValue(left.value == right.value)
                }
                else -> throw UndefinedBehaviourException("Boolean equals comparison only supported for integer values.")
            }
    }
}

fun Value.add(other: Value): Value = Op.Add.apply(this, other)

fun Value.sub(other: Value): Value = Op.Subtract.apply(this, other)

fun Value.mul(other: Value): Value = Op.Multiply.apply(this, other)

fun Value.div(other: Value): Value = Op.Divide.apply(this, other)

fun Value.and(other: Value): Value = Op.And.apply(this, other)

fun Value.or(other: Value): Value = Op.Or.apply(this, other)

fun Value.neg(): Value = Op.Neg.apply(this, null)

fun Value.gt(other: Value): Value = Op.GreaterThan.apply(this, other)

fun Value.gte(other: Value): Value = Op.GreaterThanOrEq.apply(this, other)

fun Value.lt(other: Value): Value = Op.LessThan.apply(this, other)

fun Value.lte(other: Value): Value = Op.LessThanOrEq.apply(this, other)

fun Value.eq(other: Value): Value = Op.Equals.apply(this, other)
