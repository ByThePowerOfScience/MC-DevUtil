package btpos.gradle.architectury.transformerplugin

import btpos.gradle.architectury.transformerplugin.attributes.ModuleType
import btpos.gradle.architectury.transformerplugin.attributes.PlatformType
import dev.architectury.plugin.TransformingTask
import dev.architectury.transformer.Transformer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.collections.forEach


/**
 * Configure the architectury plugin to emit dev variants
 */
class CommonPlatformTransformersPlugin : Plugin<Project> {
	lateinit var ext: PlatformTransformersPluginExtension
	
	override fun apply(project: Project) {
		ext = project.extensions.create(PlatformTransformersPluginExtension.NAME, PlatformTransformersPluginExtension::class.java)
		
		project.afterEvaluate {
			ext.apply {
				platforms.get().forEach { (platform, _) ->
					tasks.get().forEach { (jarTask, moduleTypeAttr) ->
						project.makeTransformingTask(platform, jarTask, moduleTypeAttr)
					}
				}
			}
			addCustomTransformersToAllTasks()
		}
	}
	
	/**
	 * Make tasks and configurations that apply ONLY our transformers to the given source set.
	 */
	fun Project.makeTransformingTask(platform: PlatformType, jarTask: TaskProvider<out Jar>, moduleType: ModuleType) {
		val platformName = platform.name
		
		val configName = getConfigNameForSourceTypeAndPlatform(moduleType.name, platformName)
		
		configurations.maybeCreate(configName).apply {
			isCanBeConsumed = true
			isCanBeResolved = false
			attributes {
				attribute(ModuleType.Companion.ATTRIBUTE, moduleType)
				attribute(PlatformType.ATTRIBUTE, platform)
				attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
			}
		}
		
		// Registered with no transformers because we add ours to ALL transformtasks, including this one
		val transformerTask = tasks.register<TransformingTask>(transformingTaskName(jarTask, platformName)) {
			dependsOn(jarTask)
			group = PlatformTransformersPluginExtension.TASK_GROUP
			
			input.set(jarTask.get().archiveFile)
			
			this@register.platform = platformName
			
			archiveClassifier.set(configName)
			
			artifacts.add(configName, this) // add exported variant for this config
		}
		
		transformerTask.get().archiveFile.get().asFile.takeIf { !it.exists() }?.createEmptyJar() // fix a filenotfound crash
		
		
	}
	
	
	fun Project.addCustomTransformersToAllTasks() {
		tasks.withType<TransformingTask> {
			val id = (platform ?: return@withType).lowercase()
			val customTransformers = ext.platforms.get().getOrDefault(objects.named<PlatformType>(id), listOf())
			
			// update task when the sources are changed and if the list of transformers in buildSrc changes
			inputs.file(this.input)
			inputs.property("commonTransformers", ext.commonTransformers.map { it.joinToString(";") { tf -> tf.javaClass.name } })
			inputs.property("transformers", ext.platforms.map { platform?.let { p -> it[objects.named(p)]?.joinToString(",") { tf -> tf.javaClass.name } } ?: "" })
			
			// This will be empty when we're doing the sources
			customTransformers.forEach {
				add(it) { _, _ -> }
			}
			
			ext.commonTransformers.get().forEach {
				add(it) { _, _ -> }
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun getConfigNameForSourceTypeAndPlatform(sourceTypeName: String, platformName: String): String {
			return "devTransform_${sourceTypeName}_${platformName}"
		}
		
		@JvmStatic
		fun transformingTaskName(jarTask: TaskProvider<out Jar>, platform: String): String {
			return "transform_${getConfigNameForSourceTypeAndPlatform(jarTask.get().name, platform)}"
		}
		
		@JvmStatic
		fun File.createEmptyJar() {
			parentFile.mkdirs()
			JarOutputStream(outputStream(), Manifest()).close()
		}
	}
}


interface PlatformTransformersPluginExtension {
	companion object {
		const val NAME = "devTransformers"
		
		const val TASK_GROUP = "platformTransformation"
	}
	
	/**
	 * Transformers that should be applied to all outgoing variants regardless of platform.
	 */
	val commonTransformers: ListProperty<Transformer>
	
	/**
	 * Names of the platforms this should make tasks for, and the transformers for said platform
	 *
	 * Same as with architectury.common(), but using `objects.named(platformName)` instead of raw strings cause it's easier to enforce strict naming on the backend
	 *
	 * Example:
	 * ```kotlin
	 * devTransformers {
	 *      platforms += mapOf(
	 *          objects.named(PlatformType.NEOFORGE) to listOf(NeoTransformer1()),
	 *          objects.named(PlatformType.FABRIC) to listOf(FabricTransformer1())
	 *      )
	 * }
	 * ```
	 */
	val platforms: MapProperty<PlatformType, List<Transformer>>
	
	/**
	 * Tasks to generate transformation tasks and output variants for,
	 * along with the [ModuleType] that should be applied as an attribute to the outgoing variant.
	 *
	 * Example:
	 * ```kotlin
	 * devTransformers {
	 *      tasks += mapOf(
	 *          tasks.jar to objects.named(ModuleType.MAIN),
	 *          tasks.testJar to objects.named(ModuleType.TEST)
	 *      )
	 * }
	 * ```
	 */
	val tasks: MapProperty<TaskProvider<out Jar>, ModuleType>
}