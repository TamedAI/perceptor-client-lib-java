package unit_tests

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

fun <R> (() -> R).shouldNotThrow(): R {
    return assertDoesNotThrow { this() }
}

fun <R> (() -> R).shouldThrow(): Throwable {
    return assertThrows { this() }
}

infix fun <R> Collection<R>.shouldAllSatisfy(action: (R) -> Unit) {
    forEach { x -> action(x) }
}
