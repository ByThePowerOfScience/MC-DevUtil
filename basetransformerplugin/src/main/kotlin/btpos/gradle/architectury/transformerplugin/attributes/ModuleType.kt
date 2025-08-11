package btpos.gradle.architectury.transformerplugin.attributes

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface ModuleType : Named {
	companion object {
		val ATTRIBUTE = Attribute.of("btpos.gradle.multiplatform.sourceSet_type", ModuleType::class.java)
		const val MAIN = "main"
		const val TEST = "test"
	}
}