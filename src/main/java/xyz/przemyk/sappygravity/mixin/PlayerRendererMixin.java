package xyz.przemyk.sappygravity.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(
        method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    ) // Fixes crouch offset
    private void injectGetRenderOffset(AbstractClientPlayer entity, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if (entity.getData(SappyGravity.INVERTED_GRAVITY) && entity.isCrouching()) {
            cir.setReturnValue(new Vec3(0.0, entity.getScale() * 2.0 / 16.0, 0.0));
        }
    }

    @Redirect(
        method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
        )
    ) // Fixes rotation while using elytra
    private Vec3 modifyGetViewVector(AbstractClientPlayer instance, float partialTicks) {
        Vec3 view = instance.getViewVector(partialTicks);

        if (instance.getData(SappyGravity.INVERTED_GRAVITY)) {
            return new Vec3(-view.x, -view.y, view.z);
        } else {
            return view;
        }
    }
}
