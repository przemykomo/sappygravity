package xyz.przemyk.sappygravity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow @Final private Abilities abilities;

    @Shadow protected abstract boolean isStayingOnGroundSurface();

    @Shadow protected abstract boolean isAboveGround(float maxUpStep);

    @Shadow public abstract void remove(RemovalReason reason);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
        method = "canPlayerFitWithinBlocksAndEntitiesWhen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityDimensions;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private AABB wrapMakeBoundingBox(EntityDimensions instance, Vec3 pos, Operation<AABB> original) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            AABB box = instance.makeBoundingBox(0, 0, 0);
            return new AABB(-box.minX, -box.minY, box.minZ, -box.maxX, -box.maxY, box.maxZ).move(position());
        } else {
            return original.call(instance, pos);
        }
    }

    @Unique
    private boolean sappygravity$myCanFallAtLeast(double x, double z, float distance) {
        AABB aabb = getBoundingBox();
        return level()
            .noCollision(
                this,
                new AABB(
                    aabb.minX + x,
                    aabb.minY - distance,
                    aabb.minZ + z,
                    aabb.maxX + x,
                    aabb.maxY - distance,
                    aabb.maxZ + z
                )
            );
    }

    @Inject(
        method = "maybeBackOffFromEdge",
        at = @At("HEAD"),
        cancellable = true
    ) // Fixes crouching upside down
    private void injectMaybeBackOffFromEdge(Vec3 movement, MoverType mover, CallbackInfoReturnable<Vec3> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            float maxUpStep = this.maxUpStep();
            if (!this.abilities.flying
                && movement.y > 0.0
                && (mover == MoverType.SELF || mover == MoverType.PLAYER)
                && this.isStayingOnGroundSurface()
                && this.isAboveGround(maxUpStep)) {

                double movX = movement.x;
                double movZ = movement.z;

                double movXstep = Math.signum(movX) * 0.05;
                double movZstep;

                for (movZstep = Math.signum(movZ) * 0.05; movX != 0.0 && sappygravity$myCanFallAtLeast(movX, 0.0, -maxUpStep); movX -= movXstep) {
                    if (Math.abs(movX) <= 0.05) {
                        movX = 0.0;
                        break;
                    }
                }

                while (movZ != 0.0 && sappygravity$myCanFallAtLeast(0.0, movZ, -maxUpStep)) {
                    if (Math.abs(movZ) <= 0.05) {
                        movZ = 0.0;
                        break;
                    }

                    movZ -= movZstep;
                }

                while (movX != 0.0 && movZ != 0.0 && sappygravity$myCanFallAtLeast(movX, movZ, -maxUpStep)) {
                    if (Math.abs(movX) <= 0.05) {
                        movX = 0.0;
                    } else {
                        movX -= movXstep;
                    }

                    if (Math.abs(movZ) <= 0.05) {
                        movZ = 0.0;
                    } else {
                        movZ -= movZstep;
                    }
                }

                cir.setReturnValue(new Vec3(movX, movement.y, movZ));
            } else {
                cir.setReturnValue(movement);
            }
        }
    }

    @ModifyVariable(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    private double modifyEyeYDrop(double original) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            return getEyePosition().y + 0.3;
        } else {
            return original;
        }
    }

    @WrapOperation(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(DDD)V",
            ordinal = 1
        )
    )
    private void wrapSetVelocityDrop(ItemEntity instance, double x, double y, double z, Operation<Void> original) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            original.call(instance, -x, -y, z);
        } else {
            original.call(instance, x, y, z);
        }
    }

    @ModifyVariable(
        method = "travel",
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    private double invertYVelocityForSwimming(double value) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            return -value;
        } else {
            return value;
        }
    }

}
