package xyz.przemyk.sappygravity.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(Entity.class)
public abstract class EntityMixin extends AttachmentHolder {

    @Shadow private Vec3 position;

    @Shadow private float eyeHeight;

    @Shadow public double xo;

    @Shadow public double yo;

    @Shadow public double zo;

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract float getEyeHeight();

    @Shadow @Deprecated public abstract BlockPos getOnPosLegacy();

    @Shadow private Level level;

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow @Final protected RandomSource random;

    @Shadow private EntityDimensions dimensions;

    @ModifyVariable(
        method = "move",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    ) // Fixes you moving backwards while camera is upside down
    private Vec3 modifyMoveVel(Vec3 vel, MoverType type, Vec3 pos) {
        if (getData(SappyGravity.INVERTED_GRAVITY) && (type == MoverType.SELF || type == MoverType.PLAYER)) {
            return new Vec3(-vel.x, -vel.y, vel.z);
        } else {
            return vel;
        }
    }

    @ModifyVariable(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
            ordinal = 0
        ),
        ordinal = 0,
        argsOnly = true
    ) // Makes the game consider you on ground while upside down so that you can jump
    private Vec3 modifyMoveVelForCollisionCheck(Vec3 vel) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            return new Vec3(-vel.x, -vel.y, vel.z);
        } else {
            return vel;
        }
    }

    @ModifyVariable(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
            ordinal = 0
        ),
        ordinal = 1
    ) // Makes the game consider you on ground while upside down so that you can jump
    private Vec3 modifyCollideResultForCollisionCheck(Vec3 vel) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            return new Vec3(-vel.x, -vel.y, vel.z);
        } else {
            return vel;
        }
    }

    @Inject(
        method = "getBlockPosBelowThatAffectsMyMovement",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectGetBlockPosBelow(CallbackInfoReturnable<BlockPos> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            cir.setReturnValue(BlockPos.containing(position.add(0, 0.5000001, 0)));
        }
    }

    @Inject(
        method = "getEyeY",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectGetEyeY(CallbackInfoReturnable<Double> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            cir.setReturnValue(position.y - eyeHeight);
        }
    }

    @Inject(
        method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectGetEyePosition(CallbackInfoReturnable<Vec3> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            cir.setReturnValue(position.add(0, -eyeHeight, 0));
        }
    }

    @Inject(
        method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectGetEyePosition(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            double d0 = Mth.lerp(partialTicks, this.xo, this.getX());
            double d1 = Mth.lerp(partialTicks, this.yo, this.getY()) - (double)this.getEyeHeight();
            double d2 = Mth.lerp(partialTicks, this.zo, this.getZ());
            cir.setReturnValue(new Vec3(d0, d1, d2));
        }
    }

    @Inject(
        method = "calculateViewVector",
        at = @At("RETURN"),
        cancellable = true
    ) // Makes the game highlight the correct block you are looking at
    private void injectCalculateViewVector(CallbackInfoReturnable<Vec3> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            Vec3 value = cir.getReturnValue();
            cir.setReturnValue(new Vec3(-value.x, -value.y, value.z));
        }
    }

    @Inject(
        method = "makeBoundingBox",
        at = @At("RETURN"),
        cancellable = true
    )
    private void injectMakeBoundingBox(CallbackInfoReturnable<AABB> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            AABB box = cir.getReturnValue().move(position.reverse());
            cir.setReturnValue(new AABB(-box.minX, -box.minY, box.minZ, -box.maxX, -box.maxY, box.maxZ).move(position));
        }
    }

    @Inject(
        method = "spawnSprintParticle",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectSpawnSprintParticle(CallbackInfo ci) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            ci.cancel();
            BlockPos blockpos = BlockPos.containing(position.add(0, 0.2, 0));
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (!blockstate.addRunningEffects(level, blockpos, (Entity) (Object) this)
                && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 vel = this.getDeltaMovement();
                double x = this.getX() + (this.random.nextDouble() - 0.5) * (double) this.dimensions.width();
                double z = this.getZ() + (this.random.nextDouble() - 0.5) * (double) this.dimensions.width();

                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), x, this.getY() - 0.1, z, vel.x * 4.0, -1.5, vel.z * -4.0);
            }

        }
    }

    @Inject(
        method = "getOnPosLegacy",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectGetOnPosLegacy(CallbackInfoReturnable<BlockPos> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            cir.setReturnValue(BlockPos.containing(position.add(0, 0.2, 0)));
        }
    }

    @ModifyArg(
        method = "getDirection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Direction;fromYRot(D)Lnet/minecraft/core/Direction;"
        )
    ) // Fixes placed block orientation
    private double invertRotationGetDirection(double angle) {
        return getData(SappyGravity.INVERTED_GRAVITY) ? -angle : angle;
    }

    @Inject(
        method = "canRide",
        at = @At("HEAD"),
        cancellable = true
    )
    private void injectCanRide(Entity vehicle, CallbackInfoReturnable<Boolean> cir) {
        if (getData(SappyGravity.INVERTED_GRAVITY)) {
            cir.setReturnValue(false);
        }
    }
}
