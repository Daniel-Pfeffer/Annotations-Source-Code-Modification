package social.xperience.k2

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.name.ClassId

object SharedReferences {
    var poolClassId = ClassId.fromString("social/xperience/VerificationPool")
    lateinit var poolClass: IrClass
    val generatedPoolFields: MutableSet<IrClassReference> = mutableSetOf()
}