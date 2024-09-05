package xyz.przemyk.sappygravity.mixin;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @SuppressWarnings({"PointlessBitwiseExpression"})
    @Inject(
        method = "getOverlayBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void injectGetViewBlockingState(Player player, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir) {
        if (player.getData(SappyGravity.INVERTED_GRAVITY)) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            Vec3 eyePos = player.getEyePosition();

            for (int i = 0; i < 8; i++) {
                double d0 = eyePos.x - (double) (((float)((i >> 0) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
                double d1 = eyePos.y - (double) (((float)((i >> 1) % 2) - 0.5F) * 0.1F * player.getScale());
                double d2 = eyePos.z + (double) (((float)((i >> 2) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
                pos.set(d0, d1, d2);
                BlockState blockstate = player.level().getBlockState(pos);
                if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.isViewBlocking(player.level(), pos)) {
                    cir.setReturnValue(Pair.of(blockstate, pos.immutable()));
                }
            }

            cir.setReturnValue(null);
        }
    }
}
