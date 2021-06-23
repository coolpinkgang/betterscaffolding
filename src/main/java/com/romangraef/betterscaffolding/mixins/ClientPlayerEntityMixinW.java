package com.romangraef.betterscaffolding.mixins;

import com.mojang.authlib.GameProfile;
import com.romangraef.betterscaffolding.entities.ForkliftEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixinW extends PlayerEntity {
    @Shadow
    public Input input;

    @Shadow
    private boolean riding;

    public ClientPlayerEntityMixinW(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
        throw new IllegalStateException("Constructed mixin " + getClass().getCanonicalName());
    }

    @Inject(at = @At("TAIL"), method = "tickRiding()V")
    private void ridingInputs(CallbackInfo i) {
        Entity v = getVehicle();
        if (v instanceof ForkliftEntity) {
            ForkliftEntity e = (ForkliftEntity) v;
            e.setInputs(input.pressingLeft, this.input.pressingRight, this.input.pressingForward, this.input.pressingBack);
            riding |= true;
        }
    }
}
