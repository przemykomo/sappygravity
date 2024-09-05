package xyz.przemyk.sappygravity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

//    @WrapOperation(
//        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;isEntityUpsideDown(Lnet/minecraft/world/entity/LivingEntity;)Z"
//        )
//    )
//    private boolean wrapIsEntityUpsideDown(LivingEntity entity, Operation<Boolean> original) {
//        return original.call(entity) || entity.getData(SappyGravity.INVERTED_GRAVITY);
//    }

//    @ModifyVariable(
//        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
//        at = @At(
//            value = "STORE",
//            ordinal = 0
//        )
//    )
//    private float reverseRelativeHeadRotation(float value, T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
//        if (entity.getData(SappyGravity.INVERTED_GRAVITY)) {
//            return -value;
//        } else {
//            return value;
//        }
//    }
}
