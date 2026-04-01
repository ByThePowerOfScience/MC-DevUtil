

configurations.named { when (it) {
	"apiElements", "sourcesElements", "javadocElements" -> true
	else -> it.startsWith("runtimeElements")
} }.configureEach {
	outgoing {
		capability("$group:$mod_id:$version")
	}
}
