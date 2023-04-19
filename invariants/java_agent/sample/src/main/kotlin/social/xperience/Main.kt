package social.xperience

import social.xperience.dto.Update


fun main() {
    val emptyUpdate = Update()
    doSomething(emptyUpdate)
}


fun doSomething(with: Update) {
    println(with)
}
