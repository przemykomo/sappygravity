package xyz.przemyk.sappygravity.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.przemyk.sappygravity.SappyGravity;

import javax.annotation.Nullable;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @Shadow @Nullable private LivingEntity attachedToEntity;

    @ModifyVariable(
        method = "tick",
        at = @At(
            value = "STORE"
        ),
        ordinal = 0
    ) //Fixes elytra rocket boosting
    private Vec3 modifyLookAngleTick(Vec3 value) {
        if (attachedToEntity.getData(SappyGravity.INVERTED_GRAVITY)) {
            return new Vec3(-value.x, -value.y, value.z);
        } else {
            return value;
        }
    }
}
