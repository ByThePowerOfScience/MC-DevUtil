Uses the [transformerplugin](../transformerplugin) to transform some APIs defined in DevUtil to their platform-specific versions.

For example, remapping the [IPlatformConnectRedstone](../common/src/main/kotlin/btpos/mcmods/devutil/multiplatform/api/IPlatformConnectRedstone.kt) 
interface when on NeoForge so that the `IPlatformConnectRedstone#canConnectRedstone` method is 
interpreted as Neo's `IBlockExtension#canConnectRedstone`.