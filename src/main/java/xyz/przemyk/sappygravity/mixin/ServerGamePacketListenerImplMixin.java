package xyz.przemyk.sappygravity.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @ModifyVariable(
        method = "handleMovePlayer",
        at = @At("STORE"),
        ordinal = 1
    ) // Inverts flag which checks whether player has risen and resets fall distance.
    private boolean modifyFlagHandleMovePlayer(boolean flag) {
        if (player.getData(SappyGravity.INVERTED_GRAVITY)) {
            return !flag;
        } else {
            return flag;
        }
    }
}
