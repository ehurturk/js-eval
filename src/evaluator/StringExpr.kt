package evaluator

sealed interface StringExpr {
    class Add(
        val rhs: StringExpr,
        val lhs: StringExpr,
    ) : StringExpr {
        override fun toString(): String = "$lhs + $rhs"
    }

//    class TemplateLiteral(
//        val expr: StringExpr,
//        val
//    ) : StringExpr {
//        override fun toString(): String = "`\${$expr}`"
//    }

    class Literal(
        val value: String,
    ) : StringExpr {
        override fun toString(): String = "`$value`"
    }
}

fun StringExpr.eval(store: Map<String, String>): String =
    when (this) {
        is StringExpr.Add -> lhs.eval(store) + rhs.eval(store)
        is StringExpr.Literal -> value
    }
