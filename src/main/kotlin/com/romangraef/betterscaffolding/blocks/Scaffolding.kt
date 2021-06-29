package com.romangraef.betterscaffolding.blocks

import com.romangraef.betterscaffolding.*
import com.romangraef.betterscaffolding.registries.BBlock
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
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import org.apache.commons.lang3.tuple.ImmutablePair
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

object Scaffolding {

    enum class PolePosition(val minX: Number, val minZ: Number, val sizeX: Number, val sizeZ: Number) {
        NORTH(2, 0.5, 12, 1),
        SOUTH(2, 14.5, 12, 1),
        WEST(0.5, 2, 1, 12),
        EAST(14.5, 2, 1, 12);

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
            EAST -> LegPosition.NORTH_WEST to LegPosition.SOUTH_EAST
        }
        
        fun toDirection(): Direction = when (this) {
            NORTH -> Direction.NORTH
            SOUTH -> Direction.SOUTH
            WEST  -> Direction.WEST
            EAST  -> Direction.EAST
        }
        
    }

    enum class LegPosition(val offsetX: Double, val offsetZ: Double) {
        NORTH_WEST(1.pixelAsDouble, 1.pixelAsDouble),
        NORTH_EAST(15.pixelAsDouble, 1.pixelAsDouble),
        SOUTH_WEST(1.pixelAsDouble, 15.pixelAsDouble),
        SOUTH_EAST(15.pixelAsDouble, 15.pixelAsDouble);

        fun toPolePositions(): Pair<PolePosition, PolePosition> = when (this) {
            NORTH_WEST -> PolePosition.NORTH to PolePosition.WEST
            NORTH_EAST -> PolePosition.NORTH to PolePosition.EAST
            SOUTH_WEST -> PolePosition.SOUTH to PolePosition.WEST
            SOUTH_EAST -> PolePosition.SOUTH to PolePosition.EAST
        }
    }

    enum class PlankState(
        val minX1: Number,
        val minZ1: Number,
        val minX2: Number,
        val minZ2: Number,
        val sizeX: Number,
        val sizeZ: Number
    ) : StringIdentifiable {
        NONE(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN),
        NORTH_SOUTH(2.5, 0, 8.5, 0, 5, 16),
        WEST_EAST(0, 2.5, 0, 8.5, 16, 5);

        override fun asString(): String = name.lowercase()
        fun toPolePositions(): Pair<PolePosition, PolePosition>? = when (this) {
            NONE        -> null
            NORTH_SOUTH -> PolePosition.NORTH to PolePosition.SOUTH
            WEST_EAST   -> PolePosition.WEST to PolePosition.EAST
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

        fun hasConnection(polePosition: PolePosition, state: BlockState) =
            state[polePosition.toProperty()] == PoleState.HEAD

        fun checkPolePositionOrUpdate(
            world: WorldAccess,
            blockPos: BlockPos,
            blockState: BlockState,
        ): (BlockState) -> BlockState {
            val underneath = world.getBlockState(blockPos.offset(Direction.DOWN))
            if (underneath.isAir)
                return {
                    it.with(States.POLE_N, PoleState.NONE)
                        .with(States.POLE_S, PoleState.NONE)
                        .with(States.POLE_W, PoleState.NONE)
                        .with(States.POLE_E, PoleState.NONE)
                }
            if (underneath.block == this) {
                var result: (BlockState) -> BlockState = { it }
                PolePosition.values().forEach { pos ->
                    if (hasPole(pos, blockState) > hasPole(pos, underneath))
                        result = { it.with(pos.toProperty(), PoleState.NONE) }
                }
                return result
            }
            return { it }
        }
    
        fun checkPlankPositionOrUpdate(
            world: WorldAccess,
            blockPos: BlockPos,
            blockState: BlockState
        ): (BlockState) -> BlockState {
            val sides = blockState[States.PLANK].toPolePositions()?.toList() ?: return { it }
            sides.forEach {
                val side = world.getBlockState(blockPos.offset(it.toDirection()))
                if (side.block != this) return { it.with(States.PLANK, PlankState.NONE) }
                if (side[States.PLANK] != blockState[States.PLANK])
                    return { it.with(States.PLANK, PlankState.NONE) }
            }
            return { it }
        }

        fun hasPlanks(blockState: BlockState) = blockState[States.PLANK] != PlankState.NONE

        fun hasPlank(blockState: BlockState, plankState: PlankState) = blockState[States.PLANK] == plankState

        fun checkStateOrUpdate(
            world: WorldAccess,
            blockPos: BlockPos,
            blockState: BlockState
        ): ((BlockState) -> BlockState)? {
            var result: (BlockState) -> BlockState = { it }
            var i = 0
            if (hasPoles(blockState)) {
                i++
                result = { checkPolePositionOrUpdate(world, blockPos, blockState)(result(it)) }
            }
            if (hasPlanks(blockState)){
                i++
                result = { checkPlankPositionOrUpdate(world, blockPos, blockState)(result(it)) }
            }
            if (i == 0) return null
            return result
        }
    
        private fun updateSingularPole(
            prop: EnumProperty<PoleState>,
            bs: BlockState,
            top: BlockState
        ): BlockState {
            val isTop = if (top.block == BBlock.scaffoldMicroBlock)
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
            val top = world.getBlockState(pos.offset(Direction.UP))
            bs = updateSingularPole(States.POLE_N, bs, top)
            bs = updateSingularPole(States.POLE_W, bs, top)
            bs = updateSingularPole(States.POLE_S, bs, top)
            bs = updateSingularPole(States.POLE_E, bs, top)
            if (oldBs == bs) return
            world.setBlockState(pos, bs, NOTIFY_ALL or SKIP_LIGHTING_UPDATES)
            world.blockTickScheduler.schedule(pos, this, 1)
        }

        override fun appendProperties(builder: StateManager.Builder<net.minecraft.block.Block, BlockState>) {
            builder.add(States.POLE_N, States.POLE_S, States.POLE_W, States.POLE_E, States.PLANK)
        }

        override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
            val updater = checkStateOrUpdate(world, pos, state) ?: return run { world.breakBlock(pos, true) }
            setMicroblock(world, pos) { updater(it) }
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

        fun getLegShape(legPosition: LegPosition): VoxelShape = VoxelShapes.cuboid(
            legPosition.offsetX - 0.0625,
            0.0,
            legPosition.offsetZ - 0.0625,
            legPosition.offsetX + 0.0625,
            1.0,
            legPosition.offsetZ + 0.0625
        )

        fun getPolesShape(state: BlockState): VoxelShape {
            var result = VoxelShapes.empty()
            LegPosition.values().forEach { if (hasLeg(it, state)) result += getLegShape(it) }
            return result
        }

        fun getConnectionShape(polePosition: PolePosition): VoxelShape = BVoxelShapes.cuboidB(
            polePosition.minX,
            13,
            polePosition.minZ,
            polePosition.sizeX,
            2,
            polePosition.sizeZ
        ) //TODO: make this mess a better mess

        fun getConnectionsShape(
            state: BlockState
        ): VoxelShape {
            var result = VoxelShapes.empty()
            PolePosition.values().forEach { if (hasConnection(it, state)) result += getConnectionShape(it) }
            return result
        }

        fun getPlankShape(state: BlockState) = BVoxelShapes.cuboidB(
            state[States.PLANK].minX1,
            15,
            state[States.PLANK].minZ1,
            state[States.PLANK].sizeX,
            1,
            state[States.PLANK].sizeZ
        ) + BVoxelShapes.cuboidB(
            state[States.PLANK].minX2,
            15,
            state[States.PLANK].minZ2,
            state[States.PLANK].sizeX,
            1,
            state[States.PLANK].sizeZ
        ) //TODO: make this mess a better mess

        override fun getOutlineShape(
            state: BlockState,
            world: BlockView,
            pos: BlockPos,
            context: ShapeContext
        ): VoxelShape {
            var result = VoxelShapes.empty()
            result += getPolesShape(state)
            result += getConnectionsShape(state)
            result += getPlankShape(state)
            return result
        }

        override fun getCollisionShape(
            state: BlockState,
            world: BlockView,
            pos: BlockPos,
            context: ShapeContext
        ): VoxelShape = getPlankShape(state)

        override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack =
            ItemStack(BItems.pole, 1)
    
        override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
            super.onBreak(world, pos, state, player) //TODO: do this but how?
        }
        
    }

    object Model : UnbakedModel {

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

        fun getLegModel(legPosition: LegPosition) = when (legPosition) {
            LegPosition.NORTH_WEST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_nw"), "")
            LegPosition.NORTH_EAST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_ne"), "")
            LegPosition.SOUTH_WEST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_sw"), "")
            LegPosition.SOUTH_EAST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_se"), "")
        }

        fun getConnectionModel(polePosition: PolePosition): Identifier = when (polePosition) {
            PolePosition.NORTH ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_n"), "")
            PolePosition.SOUTH ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_s"), "")
            PolePosition.WEST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_w"), "")
            PolePosition.EAST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_e"), "")
        }

        fun getPlankModel(plankState: PlankState): Identifier? = when (plankState) {
            PlankState.NONE -> null
            PlankState.NORTH_SOUTH ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_planks_ns"), "")
            PlankState.WEST_EAST ->
                ModelIdentifier(BetterScaffolding.id("block/scaffolding_planks_we"), "")
        }

        override fun getModelDependencies(): MutableCollection<Identifier> =
            (LegPosition.values().map { getLegModel(it) }
                    + PolePosition.values().map { getConnectionModel(it) }
                    + PlankState.values().mapNotNull { getPlankModel(it) }
                    ).toMutableList()

        override fun getTextureDependencies(
            unbakedModelGetter: Function<Identifier, UnbakedModel>,
            unresolvedTextureReferences: MutableSet<com.mojang.datafixers.util.Pair<String, String>>
        ): MutableCollection<SpriteIdentifier> = modelDependencies.flatMap {
            unbakedModelGetter.apply(it).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
        }.toMutableList()

        override fun bake(
            loader: ModelLoader,
            textureGetter: Function<SpriteIdentifier, Sprite>,
            rotationContainer: ModelBakeSettings,
            modelId: Identifier
        ): BakedModel = MultipartBakedModel(
            LegPosition.values().map<LegPosition, ImmutablePair<Predicate<BlockState>, BakedModel>> { pos ->
                ImmutablePair(
                    Predicate { Block.hasLeg(pos, it) },
                    loader.bake(getLegModel(pos), rotationContainer)
                )
            } + PolePosition.values().map<PolePosition, ImmutablePair<Predicate<BlockState>, BakedModel>> { pos ->
                ImmutablePair(
                    Predicate { Block.hasConnection(pos, it) },
                    loader.bake(getConnectionModel(pos), rotationContainer)
                )
            } + PlankState.values().filter { it != PlankState.NONE }
                .map<PlankState, ImmutablePair<Predicate<BlockState>, BakedModel>> { state ->
                    ImmutablePair(
                        Predicate { Block.hasPlank(it, state) },
                        loader.bake(getPlankModel(state), rotationContainer)
                    )
                }
        )

    }

}