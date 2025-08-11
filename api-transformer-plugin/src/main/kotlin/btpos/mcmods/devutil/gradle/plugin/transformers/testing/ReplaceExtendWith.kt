package btpos.mcmods.devutil.gradle.plugin.transformers.testing

import btpos.mcmods.devutil.gradle.plugin.transformers.ClassNode
import btpos.mcmods.devutil.gradle.plugin.transformers.Type

private const val EXTENDWITH_DESC = "Lorg/junit/jupiter/api/extension/ExtendWith;"

private const val AGNOSTIC_TESTRUNNER_DESC = "Lbtpos/unittest/PlatformTestRunner;"
private const val NEO_TESTRUNNER_EXTENSION_DESC = "Lbtpos/unittest/EphemeralTestServerProvider;"
private const val FABRIC_TESTRUNNER_EXTENSION_DESC = "Lbtpos/unittest/fabric/EphemeralTestServerProvider;"


fun JUnitExtendWithNeo(node: ClassNode) {
	JUnitReplaceAgnostic(node, NEO_TESTRUNNER_EXTENSION_DESC)
}

fun JUnitExtendWithFabric(node: ClassNode) {
	JUnitReplaceAgnostic(node, FABRIC_TESTRUNNER_EXTENSION_DESC)
}

private fun JUnitReplaceAgnostic(node: ClassNode, replaceWith: String) {
//	println("Checking for ExtendWith annotation")
	val extendWithAnnotation = node.visibleAnnotations?.takeIf { it.isNotEmpty() }?.firstOrNull() { it.desc == EXTENDWITH_DESC } ?: return
//	println("Found ExtendWith annotation on class ${node.name}")
	for (i in extendWithAnnotation.values.indices step 2) {
		val j = i + 1
		val propName = extendWithAnnotation.values[i]
		val propValue = extendWithAnnotation.values[j]
		
//		println("Checking $propName - $propValue")
		
		if (propName != "value")
			continue
		
//		println("Prop name is value")
		
		if (propValue == null) {
//			println("Prop value was null")
			return;
		} else if (propValue !is MutableList<*>) {
//			println("Prop value was not mutablelist")
			return
		}
//		println("Prop value is mutablelist!")
		
		@Suppress("UNCHECKED_CAST")
		val list = propValue as MutableList<Type>
		val iter = list.listIterator()
		while (iter.hasNext()) {
			val extender = iter.next()
			val typeDesc = (extender).descriptor
//			println("Extender: $extender, typedesc: $typeDesc")
			if (typeDesc == AGNOSTIC_TESTRUNNER_DESC) {
				val touse = Type.getType(replaceWith)
//				println("Removing $typeDesc and replacing with $touse")
				iter.remove()
				iter.add(touse)
			}
			break
		}
	}
}

/*
fun JUnitExtendWithNeo(): ClassVisitor2 {
	return TExtendWith(NEO_TESTRUNNER_EXTENSION_DESC)
}

fun JUnitExtendWithFabric(): ClassVisitor2 {
	return TExtendWith(FABRIC_TESTRUNNER_EXTENSION_DESC)
}

class TExtendWith(val replaceWith: String) : ClassVisitor2() {
	override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
		if (descriptor == EXTENDWITH_DESC) {
			return ReplaceExtensionTarget(api, super.visitAnnotation(descriptor, visible), replaceWith)
		}
		return super.visitAnnotation(descriptor, visible)
	}
}

private class ReplaceExtensionTarget(api: Int, delegate: AnnotationVisitor?, val replaceWith: String) : AnnotationVisitor(api, delegate) {
	override fun visitArray(name: String?): AnnotationVisitor? {
		if (name == "value") {
			return ReplaceExtensionTargetClassLiteral(api, super.visitArray(name), replaceWith)
		}
		return super.visitArray(name)
	}
}

private class ReplaceExtensionTargetClassLiteral(api: Int, delegate: AnnotationVisitor, val replaceWith: String) : AnnotationVisitor(api, delegate) {
	override fun visit(name: String?, value: Any?) {
		if (value is Type && value.descriptor == AGNOSTIC_TESTRUNNER_DESC) {
			return super.visit(name, replaceWith)
		}
	}
}*/
