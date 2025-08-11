package btpos.gradle.architectury.transformerplugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.RecordComponentVisitor
import org.objectweb.asm.TypePath

class MultiThreadedTerminalVisitor : ClassVisitor2() {
	val local: ThreadLocal<ClassVisitor> = ThreadLocal()

	override fun setDelegate(visitor: ClassVisitor?) {
		local.set(visitor)
	}

	override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor? {
		return local.get().visitTypeAnnotation(typeRef, typePath, descriptor, visible)
	}

	override fun visitSource(source: String?, debug: String?) {
		local.get().visitSource(source, debug)
	}

	override fun visitRecordComponent(name: String?, descriptor: String?, signature: String?): RecordComponentVisitor? {
		return local.get().visitRecordComponent(name, descriptor, signature)
	}

	override fun visitPermittedSubclass(permittedSubclass: String?) {
		local.get().visitPermittedSubclass(permittedSubclass)
	}

	override fun visitOuterClass(owner: String?, name: String?, descriptor: String?) {
		local.get().visitOuterClass(owner, name, descriptor)
	}

	override fun visitNestMember(nestMember: String?) {
		local.get().visitNestMember(nestMember)
	}

	override fun visitNestHost(nestHost: String?) {
		local.get().visitNestHost(nestHost)
	}

	override fun visitModule(name: String?, access: Int, version: String?): ModuleVisitor? {
		return local.get().visitModule(name, access, version)
	}

	override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String?>?): MethodVisitor? {
		return local.get().visitMethod(access, name, descriptor, signature, exceptions)
	}

	override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
		local.get().visitInnerClass(name, outerName, innerName, access)
	}

	override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor? {
		return local.get().visitField(access, name, descriptor, signature, value)
	}

	override fun visitEnd() {
		local.get().visitEnd()
	}

	override fun visitAttribute(attribute: Attribute?) {
		local.get().visitAttribute(attribute)
	}

	override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
		return local.get().visitAnnotation(descriptor, visible)
	}

	override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String?>?) {
		local.get().visit(version, access, name, signature, superName, interfaces)
	}
}