package xyz.przemyk.sappygravity;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod(SappyGravity.MODID)
public class SappyGravity {

    public static final String MODID = "sappygravity";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(MODID);

    public static final Supplier<GravityChanger> GRAVITY_CHANGER_ITEM =
        ITEMS.register("gravity_changer", () -> new GravityChanger(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<Boolean>> INVERTED_GRAVITY = ATTACHMENT_TYPES.register(
        "gravity", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Long>> ROTATION_BEGIN_TIME = ATTACHMENT_TYPES.register(
        "rotation_begin", () -> AttachmentType.builder(() -> -500L).serialize(Codec.LONG).build()
    );

    public static final long ROTATION_DURATION_TIME = 7;

    public SappyGravity(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(SappyGravity::registerPackets);
        eventBus.addListener(SappyGravity::buildContents);
        ITEMS.register(eventBus);
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("2");
        registrar.playToClient(
                InvertGravityPacket.TYPE,
                InvertGravityPacket.STREAM_CODEC,
                InvertGravityPacket::handle
        );
    }

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(GRAVITY_CHANGER_ITEM.get());
        }
    }

    public static void setGravity(Entity entity, boolean inverted) {
        if (entity.getData(INVERTED_GRAVITY) != inverted) {
            entity.setData(INVERTED_GRAVITY, inverted);
            Vec3 pos = entity.position();
            double height = entity.getBbHeight();
            entity.setPos(pos.x, pos.y + (inverted ? height : -height), pos.z);
            entity.setXRot(-entity.getXRot());
            entity.setYRot(-entity.getYRot());
            Vec3 vel = entity.getDeltaMovement();
            entity.setDeltaMovement(-vel.x, -vel.y, vel.z);

            if (entity.level().isClientSide) {
                entity.setData(ROTATION_BEGIN_TIME, entity.level().getGameTime());
            }
        }
    }

    public static long getRotationTimeDiff(Entity entity) {
        return entity.level().getGameTime() - entity.getData(ROTATION_BEGIN_TIME);
    }
}
