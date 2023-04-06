package social.xperience

import social.xperience.social.xperience.dto.Update


fun main() {
    val emptyUpdate = Update("Daniel")
    doSomething(emptyUpdate)
}


fun doSomething(with: Update) {
    println(with)
}