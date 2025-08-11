package btpos.mcmods.devutil.gradle.plugin.transformers

import dev.architectury.transformer.input.FileAccess
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.ClassVisitor
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.ClassWriter
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Opcodes.*
import dev.architectury.transformer.transformers.base.AssetEditTransformer
import dev.architectury.transformer.transformers.base.ClassEditTransformer
import dev.architectury.transformer.transformers.base.edit.TransformerContext
import java.io.File

//class TInjectModData(val packageName: String, val modId: String) : AssetEditTransformer {
//	override fun doEdit(context: TransformerContext?, output: FileAccess) {
//		output.addClass(packageName.replace('.', File.separatorChar), makeModDataClass())
//	}
//
//	fun makeModDataClass(): ByteArray {
//		val writer = ClassWriter(0)
//
//		with (writer) {
//			visit(V21, ACC_PUBLIC, "")
//		}
//
//		return writer.toByteArray()
//	}
//}