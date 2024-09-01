package xyz.przemyk.sappygravity;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InvertGravityPacket(boolean inverted, int entityID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<InvertGravityPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SappyGravity.MODID, "invert_gravity"));

    public static final StreamCodec<ByteBuf, InvertGravityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            InvertGravityPacket::inverted,
            ByteBufCodecs.INT,
            InvertGravityPacket::entityID,
            InvertGravityPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            SappyGravity.setGravity(Minecraft.getInstance().level.getEntity(entityID), inverted);
        });
    }
}
