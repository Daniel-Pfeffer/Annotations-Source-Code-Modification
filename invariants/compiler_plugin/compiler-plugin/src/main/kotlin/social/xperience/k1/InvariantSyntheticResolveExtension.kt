package social.xperience.k1

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.load.java.lazy.LazyJavaResolverContext
import org.jetbrains.kotlin.resolve.jvm.SyntheticJavaPartsProvider
import org.jetbrains.kotlin.resolve.jvm.extensions.SyntheticJavaResolveExtension

class InvariantSyntheticResolveExtension : SyntheticJavaResolveExtension {
    override fun buildProvider(): SyntheticJavaPartsProvider {
        TODO("Not yet implemented")
    }
}