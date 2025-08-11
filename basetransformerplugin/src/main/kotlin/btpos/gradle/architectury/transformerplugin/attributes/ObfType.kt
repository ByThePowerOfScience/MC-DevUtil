package btpos.gradle.architectury.transformerplugin.attributes

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface ObfType : Named {
	companion object {
		val ATTRIBUTE = Attribute.of("btpos.gradle.multiplatform.obfuscation", ObfType::class.java)
		const val OBF = "obfuscated"
		const val SRG = "srg"
		const val LOOM = "loom"
		const val DEOBF = "deobfuscated"
	}
}