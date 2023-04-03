package social.xperience.k2

import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.name.ClassId

object SharedReferences {
    var poolClassId = ClassId.fromString("social/xperience/VerificationPool")
    val generatedPoolFields: MutableSet<IrClassReference> = mutableSetOf()
}