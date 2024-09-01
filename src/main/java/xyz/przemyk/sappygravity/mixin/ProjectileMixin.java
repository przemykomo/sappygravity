package xyz.przemyk.sappygravity.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @ModifyVariable(
        method = "shootFromRotation",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private float modifyXRot(float xRot, Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        if (shooter.getData(SappyGravity.INVERTED_GRAVITY)) {
            return -xRot;
        } else {
            return xRot;
        }
    }

    @ModifyVariable(
        method = "shootFromRotation",
        at = @At("HEAD"),
        ordinal = 1,
        argsOnly = true
    )
    private float modifyYRot(float yRot, Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        if (shooter.getData(SappyGravity.INVERTED_GRAVITY)) {
            return -yRot;
        } else {
            return yRot;
        }
    }
}
