package btpos.gradle.architectury.transformerplugin

import btpos.gradle.architectury.transformerplugin.transformers.forge.TConnectRedstoneForge
import btpos.gradle.architectury.transformerplugin.transformers.testing.JUnitExtendWithFabric
import btpos.gradle.architectury.transformerplugin.transformers.testing.JUnitExtendWithNeo
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Type
import dev.architectury.transformer.transformers.base.ClassEditTransformer
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.util.stream.StreamSupport
import javax.inject.Inject

// Lets me easily swap between using the original for the docs and using the shaded one for the compat
typealias ClassNode = dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode
typealias Type = Type

// prefix = dev.architectury.transformer.shadowed.impl.

class MultiplatformPreTransformer_Forge : ClassEditTransformer {
	override fun doEdit(name: String, node: ClassNode): ClassNode {
		TConnectRedstoneForge(node)
//		JUnitExtendWithNeo(node)
		return node
	}
}

class MultiplatformPreTransformer_Fabric : ClassEditTransformer {
	override fun doEdit(name: String, node: ClassNode): ClassNode {
//		JUnitExtendWithFabric(node)
		return node
	}
}

fun getForgeTransformers(): List<ClassEditTransformer> {
	return listOf(MultiplatformPreTransformer_Forge())
}

fun getFabricTransformers(): List<ClassEditTransformer> {
	return listOf(MultiplatformPreTransformer_Fabric())
}

open class MultiplatformPreTransformer @Inject constructor(platform: String) : Action<Task> {
	val COMMON_TRANSFORMERS = listOf<ClassVisitor2>()

	fun getPlatformTransformers(platform: String): List<ClassVisitor2> {
		return when (platform) {
			"neoforge" -> listOf(
					TConnectRedstoneForge(),
					JUnitExtendWithNeo()
			)
			"fabric" -> listOf(
					JUnitExtendWithFabric()
			)
			else -> listOf()
		}
	}

	/**
	 * Assemble a big nested stack of transformers that hopefully gets optimized by the JVM into a nice functional transformation
	 */
	val combinedVisitor: ClassVisitor2? = listOf(COMMON_TRANSFORMERS, getPlatformTransformers(platform))
			.flatMap { it }
			.takeIf { it.isNotEmpty() }
			?.reduce { first, second ->
				first.setDelegate(second)
				second
			}
			?.let {
				val terminal = MultiThreadedTerminalVisitor()
				it.setDelegate(terminal)
				terminal
			}


	override fun execute(t: Task) {
		when (t) {
			is AbstractCompile -> postCompile(t.destinationDirectory)
			is KotlinJvmCompile -> postCompile(t.destinationDirectory)
		}
	}

	fun postCompile(destination: DirectoryProperty) {
		if (combinedVisitor == null)
			return
		StreamSupport.stream(destination.asFileTree.spliterator(), true)
			.filter { it.isFile && it.endsWith(".class") }
			.forEach { file ->
				val bytesIn = file.readBytes()
				val reader = ClassReader(bytesIn)
				val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS)

				combinedVisitor.setDelegate(writer)

				reader.accept(combinedVisitor, 0)

				val bytesOut = writer.toByteArray()

				if (!bytesOut.contentEquals(bytesIn))
					file.writeBytes(bytesOut)
			}
	}
}

