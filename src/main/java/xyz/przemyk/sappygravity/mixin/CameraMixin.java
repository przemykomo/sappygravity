package xyz.przemyk.sappygravity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Entity entity;

    @Shadow @Final private Quaternionf rotation;

    @Shadow private float partialTickTime;

    @Shadow private float eyeHeightOld;

    @Shadow private float eyeHeight;

    @Shadow private float yRot;

    @Shadow private float xRot;

    @Shadow private float roll;

    @WrapOperation(
        method = "setup",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
            ordinal = 0
        )
    )
    private void wrapSetPos(Camera instance, double x, double y, double z, Operation<Void> original) {
        long timeDiff = SappyGravity.getRotationTimeDiff(entity);
        boolean inverted = entity.getData(SappyGravity.INVERTED_GRAVITY);
        if (timeDiff < SappyGravity.ROTATION_DURATION_TIME) {
            double invertedY = Mth.lerp(partialTickTime, entity.yo, entity.getY()) - Mth.lerp(partialTickTime, this.eyeHeightOld, this.eyeHeight);
            if (inverted) {
                y -= entity.getBbHeight();
            } else {
                invertedY += entity.getBbHeight();
            }
            y = Mth.lerp((timeDiff + partialTickTime) / SappyGravity.ROTATION_DURATION_TIME, inverted ? y : invertedY, inverted ? invertedY : y);
            original.call(this, x, y, z);
        } else if (inverted) {
            original.call(this, x, Mth.lerp(partialTickTime, entity.yo, entity.getY()) - Mth.lerp(partialTickTime, this.eyeHeightOld, this.eyeHeight), z);
        } else {
            original.call(this, x, y, z);
        }
    }

    @Inject(
            method = "setRotation(FFF)V",
            at = @At(
                value = "INVOKE",
                target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
                shift = At.Shift.AFTER,
                remap = false
            )
    )
    private void injectSetRotation(CallbackInfo ci) {
        long timeDiff = SappyGravity.getRotationTimeDiff(entity);
        boolean inverted = entity.getData(SappyGravity.INVERTED_GRAVITY);
        if (timeDiff < SappyGravity.ROTATION_DURATION_TIME) {
            float progress = (timeDiff + partialTickTime) / SappyGravity.ROTATION_DURATION_TIME;
            if (!inverted) {
                progress = 1 - progress;
            } else {
                this.rotation.rotationYXZ((float) Math.PI + yRot * (float) (Math.PI / 180.0), xRot * (float) (Math.PI / 180.0), -roll * (float) (Math.PI / 180.0));
            }

            Vec3 axis = entity.getViewVector(partialTickTime);
            this.rotation.set(new Quaternionf().rotateAxis(
                (float) Math.toRadians(Mth.lerp(progress, 0, -180)),
                (float) axis.x, (float) axis.y, (float) axis.z).mul(this.rotation));
        } else if (inverted) {
            Quaternionf rotation = Axis.ZP.rotationDegrees(-180);
            rotation.mul(this.rotation);
            this.rotation.set(rotation);
        }
    }
}
