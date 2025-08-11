package btpos.gradle.architectury.transformerplugin.attributes

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface PlatformType : Named {
	companion object {
		val ATTRIBUTE = Attribute.of(PlatformType::class.java)
		
		const val NEOFORGE: String = "neoforge"
		const val FABRIC: String = "fabric"
	}
}