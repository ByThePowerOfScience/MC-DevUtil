import org.gradle.api.Project




val Project.mod_version: String
    get() = properties["version"] as String
val Project.group: String
    get() = properties["group"] as String
val Project.java_version: String
    get() = properties["java_version"] as String
val Project.minecraft_version: String
    get() = properties["minecraft_version"] as String
val Project.mod_name: String
    get() = properties["mod_name"] as String
val Project.mod_author: String
    get() = properties["mod_author"] as String
val Project.mod_id: String
    get() = properties["mod_id"] as String
val Project.license: String
    get() = properties["license"] as String
val Project.credits: String
    get() = properties["credits"] as String
val Project.description: String
    get() = properties["description"] as String
val Project.minecraft_version_range: String
    get() = properties["minecraft_version_range"] as String
val Project.neo_form_version: String
    get() = properties["neo_form_version"] as String
val Project.parchment_minecraft: String
    get() = properties["parchment_minecraft"] as String
val Project.parchment_version: String
    get() = properties["parchment_version"] as String
val Project.fabric_version: String
    get() = properties["fabric_version"] as String
val Project.fabric_loader_version: String
    get() = properties["fabric_loader_version"] as String
val Project.neoforge_version: String
    get() = properties["neoforge_version"] as String
val Project.neoforge_loader_version_range: String
    get() = properties["neoforge_loader_version_range"] as String
//val Project.devutil_version: String
//    get() = this.the<LibrariesForLibs>().versions.devutil as String