package xyz.przemyk.sappygravity.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void rotateEntityUpsideDown(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        long timeDiff = SappyGravity.getRotationTimeDiff(entity);
        boolean inverted = entity.getData(SappyGravity.INVERTED_GRAVITY);
        if (timeDiff < SappyGravity.ROTATION_DURATION_TIME) {
            poseStack.pushPose();
            double height = entity.getBbHeight() / 2;
            poseStack.translate(0, height * (inverted ? -1 : 1), 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp((timeDiff + partialTicks) / SappyGravity.ROTATION_DURATION_TIME, inverted ? 0 : -180, inverted ? -180 : 0)
            ).conjugate());
            poseStack.translate(0, -height, 0);
        } else if (inverted) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(-180).conjugate());
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 1
        )
    )
    private void rotateEntityUpsideDownPop(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity.getData(SappyGravity.INVERTED_GRAVITY) || SappyGravity.getRotationTimeDiff(entity) < SappyGravity.ROTATION_DURATION_TIME) {
            poseStack.popPose();
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void rotateEntityUpsideDownAgain(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity.getData(SappyGravity.INVERTED_GRAVITY)) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-180).conjugate());
        }
    }

    @ModifyVariable(
        method = "renderHitbox",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
            ordinal = 0
        ),
        ordinal = 0
    )
    private static AABB modifyRenderHitbox(AABB value, PoseStack poseStack, VertexConsumer buffer, Entity entity, float red, float green, float blue, float alpha) {
        if (entity.getData(SappyGravity.INVERTED_GRAVITY)) {
            return new AABB(-value.minX, -value.minY, value.minZ, -value.maxX, -value.maxY, value.maxZ);
        } else {
            return value;
        }
    }

    @Redirect(
        method = "renderHitbox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private static Vec3 redirectViewVector(Entity instance, float partialTicks) {
        if (instance.getData(SappyGravity.INVERTED_GRAVITY)) {
            Vec3 view = instance.getViewVector(partialTicks);
            return new Vec3(-view.x, -view.y, view.z);
        } else {
            return instance.getViewVector(partialTicks);
        }
    }
}
