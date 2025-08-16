@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.vanilla.destructuring

import net.minecraft.core.BlockPos
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.world.level.ChunkPos

inline operator fun BlockPos.component1() = this.x
inline operator fun BlockPos.component2() = this.y
inline operator fun BlockPos.component3() = this.z

inline operator fun ChunkPos.component1() = this.x
inline operator fun ChunkPos.component2() = this.z

inline operator fun Vec3i.component1() = x
inline operator fun Vec3i.component2() = y
inline operator fun Vec3i.component3() = z

inline operator fun Position.component1() = x()
inline operator fun Position.component2() = y()
inline operator fun Position.component3() = z()