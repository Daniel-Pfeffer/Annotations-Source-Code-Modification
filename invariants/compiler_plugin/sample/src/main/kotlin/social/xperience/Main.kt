package social.xperience

import social.xperience.dto.Update
import social.xperience.validator.DoSomethingVerifier


fun main() {
    val emptyUpdate = Update(lastname = "Pfeffer")
    doSomething(emptyUpdate)
}

@Holds(DoSomethingVerifier::class)
fun doSomething(with: Update) {
    println(with)
}
