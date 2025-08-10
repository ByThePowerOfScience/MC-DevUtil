package btpos.gradle.architectury.transformerplugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * Literally just a class visitor where we can set the delegate after init so we can fold them easier
 */
open class ClassVisitor2 : ClassVisitor(Opcodes.ASM5) {
	open fun setDelegate(visitor: ClassVisitor?) {
		this.cv = visitor
	}
}