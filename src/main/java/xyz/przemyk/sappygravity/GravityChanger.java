package xyz.przemyk.sappygravity;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class GravityChanger extends Item {

    public GravityChanger(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        player.getCooldowns().addCooldown(this, (int) SappyGravity.ROTATION_DURATION_TIME);
        SappyGravity.setGravity(player, !player.getData(SappyGravity.INVERTED_GRAVITY));
        if (!level.isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new InvertGravityPacket(player.getData(SappyGravity.INVERTED_GRAVITY), player.getId()));
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
