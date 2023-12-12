/*
Copyright 2023 TamedAI GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
