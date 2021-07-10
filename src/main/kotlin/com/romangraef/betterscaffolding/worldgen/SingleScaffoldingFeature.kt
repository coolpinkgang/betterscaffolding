package com.romangraef.betterscaffolding.worldgen

import com.romangraef.betterscaffolding.blocks.Scaffolding
import com.romangraef.betterscaffolding.choose
import com.romangraef.betterscaffolding.items.PlankItem.Companion.plankDirection
import com.romangraef.betterscaffolding.items.toPolePosition
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.util.FeatureContext
import kotlin.random.asKotlinRandom

class SingleScaffoldingFeature : Feature<DefaultFeatureConfig>(DefaultFeatureConfig.CODEC) {
    override fun generate(context: FeatureContext<DefaultFeatureConfig>?): Boolean {
        val ctx = context ?: return false
        val topPosition = ctx.world.getTopPosition(Heightmap.Type.WORLD_SURFACE, ctx.origin)
        val dir = ctx.random.asKotlinRandom().choose(Direction.Axis.X, Direction.Axis.Z)
        val neg = Direction.from(dir, Direction.AxisDirection.NEGATIVE)
        val pos = neg.opposite
        fun placeSide(base: BlockPos, dir: Direction) {
            ctx.world.setBlockState(
                base,
                BBlock.scaffoldMicroBlock.defaultState
                    .with(dir.toPolePosition().toProperty(), Scaffolding.PoleState.BASE),
                0
            )
            ctx.world.setBlockState(
                base.up(),
                BBlock.scaffoldMicroBlock.defaultState
                    .with(dir.toPolePosition().toProperty(), Scaffolding.PoleState.HEAD)
                    .with(Scaffolding.States.PLANK, dir.plankDirection),
                0
            )
        }
        placeSide(topPosition.offset(neg), neg)
        placeSide(topPosition.offset(pos), pos)
        ctx.world.setBlockState(
            topPosition.up(),
            BBlock.scaffoldMicroBlock.defaultState
                .with(Scaffolding.States.PLANK, neg.plankDirection),
            0
        )
        return true
    }
}


