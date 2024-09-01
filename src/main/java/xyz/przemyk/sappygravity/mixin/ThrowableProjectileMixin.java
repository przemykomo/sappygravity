package xyz.przemyk.sappygravity.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin {

    @ModifyArgs(
        method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;)V"
        )
    )
    private static void setProperEyeY(Args args, EntityType<? extends ThrowableProjectile> type, LivingEntity owner, Level world) {
        if (owner.getData(SappyGravity.INVERTED_GRAVITY)) {
            args.set(2, owner.getEyePosition().y + 0.1);
        }
    }
}
