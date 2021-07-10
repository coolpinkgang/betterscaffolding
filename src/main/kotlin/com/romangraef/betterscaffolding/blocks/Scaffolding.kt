package com.romangraef.betterscaffolding.blocks

import com.romangraef.betterscaffolding.*
import com.romangraef.betterscaffolding.items.PlankItem.Companion.plankDirection
import com.romangraef.betterscaffolding.registries.BItems
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelResourceProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import org.apache.commons.lang3.tuple.ImmutablePair
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

object Scaffolding {

    enum class PolePosition {
        NORTH,
        SOUTH,
        WEST,
        EAST;

        fun getVoxelShape(blockState: BlockState): VoxelShape = buildShape { shape ->
            if (blockState[this.toProperty()] == PoleState.HEAD) shape += BVoxelShapes.cuboidB(
                0.5, 13, 2, 1, 2, 14,
                negateX = this == SOUTH || this == EAST,
                switchXZ = this == NORTH || this == SOUTH
            )
            if (blockState[this.toProperty()] != PoleState.NONE)
                this.toLegPositions().toList().forEach { shape += it.getVoxelShape() }
        }

        fun getConnectionModelId(): ModelIdentifier {
            val directions = toShorthand()
            return ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_$directions"), "")
        }

        fun toProperty() = when (this) {
            NORTH -> States.POLE_N
            SOUTH -> States.POLE_S
            WEST -> States.POLE_W
            EAST -> States.POLE_E
        }

        fun toLegPositions(): Pair<LegPosition, LegPosition> = when (this) {
            NORTH -> LegPosition.NORTH_WEST to LegPosition.NORTH_EAST
            SOUTH -> LegPosition.SOUTH_WEST to LegPosition.SOUTH_EAST
            WEST -> LegPosition.NORTH_WEST to LegPosition.SOUTH_WEST
            EAST -> LegPosition.NORTH_EAST to LegPosition.SOUTH_EAST
        }

        fun toDirection(): Direction = when (this) {
            NORTH -> Direction.NORTH
            SOUTH -> Direction.SOUTH
            WEST -> Direction.WEST
            EAST -> Direction.EAST
        }

    }

    enum class LegPosition {
        NORTH_WEST,
        NORTH_EAST,
        SOUTH_WEST,
        SOUTH_EAST;

        fun getModelId(): ModelIdentifier {
            val directions = toPolePositions().toList().joinToString("") { it.toShorthand() }
            return ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_$directions"), "")
        }

        fun getVoxelShape() = BVoxelShapes.cuboidB(
            0, 0, 0, 2, 16, 2,
            negateX = this == NORTH_EAST || this == SOUTH_EAST,
            negateZ = this == SOUTH_WEST || this == SOUTH_EAST
        )

        fun toPolePositions(): Pair<PolePosition, PolePosition> = when (this) {
            NORTH_WEST -> PolePosition.NORTH to PolePosition.WEST
            NORTH_EAST -> PolePosition.NORTH to PolePosition.EAST
            SOUTH_WEST -> PolePosition.SOUTH to PolePosition.WEST
            SOUTH_EAST -> PolePosition.SOUTH to PolePosition.EAST
        }
    }

    enum class PlankState : StringIdentifiable {
        NONE,
        NORTH_SOUTH,
        WEST_EAST,
        STAIR_NORTH_SOUTH,
        STAIR_SOUTH_NORTH,
        STAIR_WEST_EAST,
        STAIR_EAST_WEST;

        fun isPlank() = this == NORTH_SOUTH || this == WEST_EAST

        fun isStairs() = !this.isPlank() && this != NONE

        fun getModelId(): ModelIdentifier? {
            if (this == NONE) return null
            val type = if (isPlank()) "planks" else "stairs"
            val direction = toPolePositions()!!.toList().joinToString("") { it.toShorthand() }
            return ModelIdentifier(BetterScaffolding.id("block/scaffolding_${type}_$direction"), "")
        }

        override fun asString(): String = name.lowercase()

        fun toPolePositions(): Pair<PolePosition, PolePosition>? = when (this) {
            NONE -> null
            NORTH_SOUTH -> PolePosition.NORTH to PolePosition.SOUTH
            WEST_EAST -> PolePosition.WEST to PolePosition.EAST
            STAIR_NORTH_SOUTH -> PolePosition.NORTH to PolePosition.SOUTH
            STAIR_SOUTH_NORTH -> PolePosition.SOUTH to PolePosition.NORTH
            STAIR_WEST_EAST -> PolePosition.WEST to PolePosition.EAST
            STAIR_EAST_WEST -> PolePosition.EAST to PolePosition.WEST
        }

        fun getVoxelShapes(): VoxelShape = buildShape { shape ->
            if (this == NORTH_SOUTH || this == WEST_EAST) {
                listOf(true, false).forEach {
                    shape += BVoxelShapes.cuboidB(
                        2.5, 15, 0, 5, 1, 16,
                        negateX = it,
                        switchXZ = this == WEST_EAST
                    )
                }
            } else if (this != NONE) {
                (0..3).forEach {
                    shape += BVoxelShapes.cuboidB(
                        it * 4, (it + 1) * 4 - 1, 1, 4, 1, 14,
                        negateX = this == STAIR_SOUTH_NORTH || this == STAIR_EAST_WEST,
                        switchXZ = this == STAIR_NORTH_SOUTH || this == STAIR_SOUTH_NORTH
                    )
                }
            }
        }
    }

    enum class PoleState : StringIdentifiable {
        NONE,
        BASE,
        HEAD;

        override fun asString(): String = name.lowercase()
    }

    object States {
        val POLE_N by enumProperty<PoleState>()
        val POLE_S by enumProperty<PoleState>()
        val POLE_W by enumProperty<PoleState>()
        val POLE_E by enumProperty<PoleState>()
        val PLANK by enumProperty<PlankState>()
    }

    object Block : net.minecraft.block.Block(Settings.copy(Blocks.SCAFFOLDING)) {

        init {
            defaultState = stateManager.defaultState
                .with(States.POLE_N, PoleState.NONE)
                .with(States.POLE_S, PoleState.NONE)
                .with(States.POLE_W, PoleState.NONE)
                .with(States.POLE_E, PoleState.NONE)
                .with(States.PLANK, PlankState.NONE)
        }

        fun hasPole(polePosition: PolePosition, blockState: BlockState) =
            blockState[polePosition.toProperty()] != PoleState.NONE

        fun hasLeg(legPosition: LegPosition, blockState: BlockState) =
            legPosition.toPolePositions().toList().any { hasPole(it, blockState) }

        fun hasPoles(blockState: BlockState) =
            PolePosition.values().any { hasPole(it, blockState) }

        fun hasStairs(blockState: BlockState) = blockState[States.PLANK].isStairs()

        fun hasConnection(polePosition: PolePosition, state: BlockState) =
            state[polePosition.toProperty()] == PoleState.HEAD

        fun checkPolePositionOrUpdate(
            world: WorldAccess,
            blockPos: BlockPos,
            blockState: BlockState,
        ): BlockState {
            val underneath = world.getBlockState(blockPos.offset(Direction.DOWN))
            if (underneath.isAir)
                return blockState.with(States.POLE_N, PoleState.NONE)
                    .with(States.POLE_S, PoleState.NONE)
                    .with(States.POLE_W, PoleState.NONE)
                    .with(States.POLE_E, PoleState.NONE)
            if (underneath.block == this) {
                return PolePosition.values().fold(blockState) { state, pos ->
                    if (hasPole(pos, blockState) > hasPole(pos, underneath))
                        state.with(pos.toProperty(), PoleState.NONE)
                    else state
                }
            }
            return blockState
        }

        fun checkPlankPositionOrUpdate(
            world: WorldAccess,
            blockPos: BlockPos,
            blockState: BlockState
        ): BlockState {
            val sides = blockState[States.PLANK].toPolePositions()?.toList() ?: return blockState
            val withoutPlanks = blockState.with(States.PLANK, PlankState.NONE)
            sides.forEach {
                if (hasPole(it, blockState)) return@forEach
                val side = world.getBlockState(blockPos.offset(it.toDirection()))
                if (side.block != this) return withoutPlanks
                if (side[States.PLANK] != blockState[States.PLANK])
                    return withoutPlanks
            }
            return blockState
        }

        fun hasPlanks(blockState: BlockState) = blockState[States.PLANK] != PlankState.NONE

        fun hasPlank(blockState: BlockState, plankState: PlankState) = blockState[States.PLANK] == plankState

        fun getUpdatedState(
            world: WorldAccess,
            blockPos: BlockPos,
            blockState: BlockState
        ): BlockState {
            var result: BlockState = blockState
            var i = 0
            if (hasPoles(blockState)) {
                i++
                result = checkPolePositionOrUpdate(world, blockPos, result)
                result = updateAllPoles(world.getBlockState(blockPos.offset(Direction.UP)), result)
            }
            if (hasStairs(blockState)) {
                val (down, up) = blockState[States.PLANK].toPolePositions()!!
                result = updateValidStairAttachment(
                    blockState,
                    world.getBlockState(blockPos.down().offset(down.toDirection())),
                    world.getBlockState(blockPos.offset(up.toDirection())),
                    world.getBlockState(blockPos.down()),
                    world.getBlockState(blockPos.offset(up.toDirection()).up())
                )
            }
            if (hasPlanks(blockState)) {
                i++
                if (blockState[States.PLANK].isPlank())
                    result = checkPlankPositionOrUpdate(world, blockPos, result)
            }
            if (i == 0) return Blocks.AIR.defaultState
            return result
        }

        private fun updateValidStairAttachment(
            blockState: BlockState,
            nextBelow: BlockState,
            nextEven: BlockState,
            below: BlockState,
            nextAbove: BlockState
        ): BlockState {
            if (!hasStairs(blockState))
                return blockState
            val (down, up) = blockState[States.PLANK].toPolePositions()!!
            val plankDir = up.toDirection().plankDirection
            val WITHOUT_STAIRS = blockState.with(States.PLANK, PlankState.NONE)
            if (nextAbove.block != this || nextAbove[States.PLANK] != blockState[States.PLANK]) {
                if (!hasPole(up, blockState)) {
                    if (nextEven.isAir) return WITHOUT_STAIRS
                    if (nextEven.block == this) {
                        if (!hasPlank(nextEven, plankDir))
                            return WITHOUT_STAIRS
                    }
                }
            }
            if (nextBelow.block != this || (!hasPlank(
                    nextBelow,
                    plankDir
                ) && nextBelow[States.PLANK] != blockState[States.PLANK])
            ) {
                if (below.isAir) return WITHOUT_STAIRS
                if (below.block == this) {
                    if (!hasPole(down, below))
                        return WITHOUT_STAIRS
                }
            }
            return blockState
        }

        private fun updateAllPoles(top: BlockState, blockState: BlockState): BlockState {
            var bs = blockState
            bs = updateSingularPole(States.POLE_N, bs, top)
            bs = updateSingularPole(States.POLE_W, bs, top)
            bs = updateSingularPole(States.POLE_S, bs, top)
            bs = updateSingularPole(States.POLE_E, bs, top)
            return bs
        }

        private fun updateSingularPole(
            prop: EnumProperty<PoleState>,
            bs: BlockState,
            top: BlockState
        ): BlockState {
            val isTop = if (top.block == this)
                top[prop] == PoleState.NONE
            else
                true
            val isTopOrPlanks = hasPlanks(bs) || isTop
            return bs.with(
                prop,
                if (bs[prop] != PoleState.NONE)
                    if (isTopOrPlanks)
                        PoleState.HEAD
                    else PoleState.BASE
                else PoleState.NONE
            )
        }

        fun setMicroblock(world: WorldAccess, pos: BlockPos, updater: (BlockState) -> BlockState) {
            var bs = world.getBlockState(pos)
            val oldBs = bs
            if (bs == null || bs.isAir) bs = this.defaultState
            if (bs.block != this) throw IllegalStateException("Invalid initial block state during microblock update: $bs")
            bs = updater(bs)
            if (bs.block != this) throw IllegalStateException("Invalid updated block state during microblock update: $bs")
            if (oldBs == bs) return
            world.setBlockState(pos, bs, NOTIFY_ALL or SKIP_LIGHTING_UPDATES)
            world.blockTickScheduler.schedule(pos, this, 1)
        }

        override fun appendProperties(builder: StateManager.Builder<net.minecraft.block.Block, BlockState>) {
            builder.add(States.POLE_N, States.POLE_S, States.POLE_W, States.POLE_E, States.PLANK)
        }

        override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
            val updatedState = getUpdatedState(world, pos, state)
            if (updatedState != state)
                world.setBlockState(pos, updatedState)
        }

        override fun getStateForNeighborUpdate(
            state: BlockState,
            direction: Direction,
            neighborState: BlockState,
            world: WorldAccess,
            pos: BlockPos,
            neighborPos: BlockPos
        ): BlockState {
            world.blockTickScheduler.schedule(pos, this, 1)
            return state
        }

        fun getPolesShape(state: BlockState): VoxelShape = buildShape { shape ->
            PolePosition.values().forEach { shape += it.getVoxelShape(state) }
        }

        fun getPlankShape(state: BlockState) = state[States.PLANK].getVoxelShapes()

        override fun getOutlineShape(
            state: BlockState,
            world: BlockView,
            pos: BlockPos,
            context: ShapeContext
        ): VoxelShape = buildShape { shape ->
            shape += getPolesShape(state)
            shape += getPlankShape(state)
        }

        override fun getCollisionShape(
            state: BlockState,
            world: BlockView,
            pos: BlockPos,
            context: ShapeContext
        ): VoxelShape = getPlankShape(state)

        override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack =
            ItemStack(BItems.pole, 1)

        override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
            if (hasStairs(state)) {
                val (down, up) = state[States.PLANK].toPolePositions()!!
                listOf(pos.offset(down.toDirection()).down(), pos.offset(up.toDirection()).up())
                    .filter { world.getBlockState(it).block == this }
                    .forEach { world.blockTickScheduler.schedule(it, this, 1) }
            }
            super.onBreak(world, pos, state, player) //TODO: do this but how?
        }

    }

    object Model : UnbakedModel {
        val MAIN_MODEL = BetterScaffolding.id("block/main")

        object Provider : ModelResourceProvider {
            val modelResource = BetterScaffolding.id("block/micro_block")
            override fun loadModelResource(resourceId: Identifier?, context: ModelProviderContext?): UnbakedModel? {
                if (resourceId == modelResource) {
                    BetterScaffolding.logger.info("Providing model resource ScaffoldMicroBlockModel")
                    return Model
                }
                return null
            }
        }

        override fun getModelDependencies(): MutableCollection<Identifier> =
            (LegPosition.values().map { it.getModelId() }
                    + PolePosition.values().map { it.getConnectionModelId() }
                    + PlankState.values().mapNotNull { it.getModelId() }
                    + listOf(MAIN_MODEL)
                    ).toMutableList()

        override fun getTextureDependencies(
            unbakedModelGetter: Function<Identifier, UnbakedModel>,
            unresolvedTextureReferences: MutableSet<com.mojang.datafixers.util.Pair<String, String>>
        ): MutableCollection<SpriteIdentifier> = (modelDependencies.flatMap {
            unbakedModelGetter.apply(it).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
        }).toMutableList()

        override fun bake(
            loader: ModelLoader,
            textureGetter: Function<SpriteIdentifier, Sprite>,
            rotationContainer: ModelBakeSettings,
            modelId: Identifier
        ): BakedModel = MultipartBakedModel(
            listOf(
                ImmutablePair<Predicate<BlockState>, BakedModel>(
                    Predicate { false },
                    loader.bake(MAIN_MODEL, rotationContainer)
                )
            ) +
                    LegPosition.values().map<LegPosition, ImmutablePair<Predicate<BlockState>, BakedModel>> { pos ->
                        ImmutablePair(
                            Predicate { Block.hasLeg(pos, it) },
                            loader.bake(pos.getModelId(), rotationContainer)
                        )
                    } + PolePosition.values()
                .map<PolePosition, ImmutablePair<Predicate<BlockState>, BakedModel>> { pos ->
                    ImmutablePair(
                        Predicate { Block.hasConnection(pos, it) },
                        loader.bake(pos.getConnectionModelId(), rotationContainer)
                    )
                } + PlankState.values().filter { it != PlankState.NONE }
                .map<PlankState, ImmutablePair<Predicate<BlockState>, BakedModel>> { state ->
                    ImmutablePair(
                        Predicate { Block.hasPlank(it, state) },
                        loader.bake(state.getModelId(), rotationContainer)
                    )
                }
        )
    }

}
