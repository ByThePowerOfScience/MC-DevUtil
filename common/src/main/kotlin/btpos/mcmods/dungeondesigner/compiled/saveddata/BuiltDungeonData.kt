package btpos.mcmods.dungeondesigner.compiled.saveddata

import btpos.mcmods.devutil.common.dsl.CodecBuilderMacros
import btpos.mcmods.devutil.common.ext.vanilla.destructuring.component1
import btpos.mcmods.devutil.common.ext.vanilla.destructuring.component2
import btpos.mcmods.devutil.common.ext.vanilla.world.modifyBlockAndUpdate
import btpos.mcmods.devutil.common.ext.vanilla.world.with
import btpos.mcmods.devutil.common.macros.fastMapOf
import btpos.mcmods.devutil.common.util.serialization.Serialization
import btpos.mcmods.dungeondesigner.POWERED
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import com.google.common.collect.ImmutableMap
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.Object2BooleanMap
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.AABB
import java.util.UUID

/**
 * Raw serialized form of the Dungeon's logical data.
 *
 * All positions, including bounding boxes, are stored as an offset from the Dungeon Nexus's position.
 */
data class DungeonTemplateNbt(
	val dungeonBoundingBox: AABB,
	val rooms: List<AABB>,
	/**
		 * List of all triggers in the dungeon.
		 *
		 * Key: Bounding box of the trigger.
		 * Value: Positions of the TriggerBlocks that will be powered if a player enters the bounding box.
		 */
		val triggers: List<Pair<AABB, List<BlockPos>>>,
	/**
		 * List of all flags in the dungeon.
		 *
		 * Key: Name of the flag.
		 * Value: Positions of the FlagBlocks that will be powered if the flag is set to true.
		 */
		val flags: List<Pair<String, List<BlockPos>>>
) {
	companion object {
		val CODEC = with (CodecBuilderMacros) {
			RecordCodecBuilder.create {
				it.group(
						"bbox" - Serialization.CODEC_AABB_BLOCK gets DungeonTemplateNbt::dungeonBoundingBox,
						"rooms" - Codec.list(Serialization.CODEC_AABB_BLOCK) gets DungeonTemplateNbt::rooms,
						"triggers" - Codec.compoundList(Serialization.CODEC_AABB_BLOCK, BlockPos.CODEC.listOf()) gets DungeonTemplateNbt::triggers,
						"flags" - Codec.compoundList(Codec.STRING, BlockPos.CODEC.listOf()) gets DungeonTemplateNbt::flags,
				).apply(it, ::DungeonTemplateNbt)
			}
		}
	}
	
	fun convertToAbsoluteCoords(anchorPos: BlockPos): DungeonTemplateNbt {
		val newBBox = dungeonBoundingBox.move(anchorPos)
		val newRooms = rooms.map { it.move(anchorPos) }
		val newTriggers = triggers.map { (aabb, positions) ->
			Pair(aabb.move(anchorPos), positions.map { it.offset(anchorPos) })
		}
		val newFlags = flags.map { (name, positions) ->
			Pair(name, positions.map { it.offset(anchorPos) })
		}
		return DungeonTemplateNbt(newBBox, newRooms, newTriggers, newFlags)
	}
}

typealias FlagName = String

/**
 *  TODO figure out if we should move flagValues to the Nexus tile entity or something,
 *      since it being here means we have to figure out a way to store that info separately,
 *      or if we should just stick to this being its own separate dynamic object type from the Template form
 */
data class DeserializedDungeonNbt private constructor(
	val dungeonBoundingBox: AABB,
	val rooms: List<Room>,
	val flagValues: Object2BooleanMap<FlagName>,
	val flagsToBlocks: Map<FlagName, List<BlockPos>>
) {
	companion object {
		fun deserialize(nexusPosition: BlockPos, serialized: DungeonTemplateNbt): DeserializedDungeonNbt {
			val (dungeonBBox, rawRooms, rawTriggers, rawFlags) = serialized.convertToAbsoluteCoords(nexusPosition)
			
			val allTriggers = rawTriggers.map { (aabb, targets) -> Trigger(aabb, targets) }
			
			
			val flagValues = Object2BooleanOpenHashMap<FlagName>()
			val flagBlocks = ImmutableMap.Builder<FlagName, List<BlockPos>>()
			
			for ((name, targets) in rawFlags) {
				flagValues.put(name, false)
				flagBlocks.put(name, targets)
			}
			
			val triggersByRoom = rawRooms.map { roomBB ->
				val triggersInRoom = mutableListOf<Trigger>()
				for (trigger in allTriggers) {
					if (roomBB.intersects(trigger.boundingBox)) {
						triggersInRoom += trigger
					}
				}
				Room(roomBB, triggersInRoom)
			}
			
			return DeserializedDungeonNbt(dungeonBBox, triggersByRoom, flagValues, flagBlocks.build())
		}
		
		@JvmField
		val CODEC = RecordCodecBuilder.create {
			it.group(
					Serialization.CODEC_AABB.fieldOf("dungeon_bounds").forGetter(DeserializedDungeonNbt::dungeonBoundingBox),
					Room.CODEC.listOf().fieldOf("rooms").forGetter(DeserializedDungeonNbt::rooms),
					Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("flag_values").forGetter(DeserializedDungeonNbt::flagValues),
					Codec.unboundedMap(Codec.STRING, BlockPos.CODEC.listOf()).fieldOf("flag_listeners").forGetter(DeserializedDungeonNbt::flagsToBlocks)
			).apply(it) { bounds, rooms, flagValues, flagListeners ->
				DeserializedDungeonNbt(bounds, rooms, Object2BooleanOpenHashMap(flagValues), flagListeners)
			}
		}
	}
	
	fun tick(level: ServerLevel) {
		// Find players in the dungeon dimension
		val players = level.getPlayers { p -> p.position() in dungeonBoundingBox }
		
		checkForActiveTriggers(players, level)
		checkForInactiveTriggers(level)
	}
	
	val activeTriggers = fastMapOf<Trigger, MutableSet<UUID>>().withDefault { ObjectOpenHashSet() }
	
	/**
	 *
	 */
	private fun checkForActiveTriggers(players: List<ServerPlayer>, level: ServerLevel) {
		// Check every player to see if they're in this dungeon
		for (player in players) {
			// Narrow down which trigger(s) the player is in, if any, and power the connected trigger-listener blocks
			rooms.asSequence()
				.filter { room -> player.position() in room.boundingBox }
				.flatMap { it.containedTriggers }
				.filter { trigger -> player.position() in trigger.boundingBox }
				.forEach {
					activeTriggers.getValue(it).add(player.uuid)
					it.powerListeners(level)
				}
		}
	}
	
	private fun checkForInactiveTriggers(level: ServerLevel) {
		activeTriggers.mapNotNull { (trigger, activators) ->
			activators.removeAll { uuid ->
				level.getPlayerByUUID(uuid).let { it == null || it.position() !in trigger.boundingBox }
			}
			
			return@mapNotNull if (activators.isEmpty()) trigger else null
		}.forEach {
			activeTriggers.remove(it)
			it.unpowerListeners(level)
		}
	}
	
	fun setFlag(level: ServerLevel, flag: FlagName, value: Boolean) {
		val oldValue = this.flagValues.put(flag, value)
		if (oldValue != value)
			flagsToBlocks[flag]?.forEach { pos ->
				level.modifyBlockAndUpdate(pos) { it.with(POWERED, value) }
			}
	}
}

/**
 * A wider bounding box holding a number of smaller triggers,
 *  used to limit the amount of triggers we check each player against per tick.
 */
data class Room(val boundingBox: AABB, val containedTriggers: List<Trigger>) {
	companion object {
		@JvmField
		val CODEC: Codec<Room> = RecordCodecBuilder.create {
			it.group(
					Serialization.CODEC_AABB.fieldOf("bounds").forGetter(Room::boundingBox),
					Trigger.CODEC.listOf().fieldOf("triggers").forGetter(Room::containedTriggers)
			).apply(it, ::Room)
		}
	}
}

data class Trigger(val boundingBox: AABB, val listeners: List<BlockPos>) {
	fun powerListeners(level: ServerLevel) {
		listeners.forEach { pos ->
			val state = level.getBlockState(pos)
			val newState = when (state.block) {
				ModBlocks_Builder.TRIGGER_BLOCK -> state.with(POWERED, true)
				else -> return@forEach
			}
			if (newState != state)
				level.setBlockAndUpdate(pos, newState)
		}
	}
	
	fun unpowerListeners(level: ServerLevel) {
		listeners.forEach { pos ->
			val state = level.getBlockState(pos)
			val newState = when (state.block) {
				ModBlocks_Builder.TRIGGER_BLOCK -> state.with(POWERED, false)
				else -> return@forEach
			}
			if (newState != state)
				level.setBlockAndUpdate(pos, newState)
		}
	}
	
	companion object {
		val CODEC: Codec<Trigger> = RecordCodecBuilder.create {
			it.group(
					Serialization.CODEC_AABB.fieldOf("bounds").forGetter(Trigger::boundingBox),
					BlockPos.CODEC.listOf().fieldOf("listeners").forGetter(Trigger::listeners)
			).apply(it, ::Trigger)
		}
	}
}