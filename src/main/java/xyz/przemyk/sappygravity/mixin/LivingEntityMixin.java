package xyz.przemyk.sappygravity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
        method = "travel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"
        )
    ) //Fixes elytra flight
    private Vec3 wrapGetLookAngle(LivingEntity instance, Operation<Vec3> original) {
        Vec3 vec = original.call(instance);
        if (instance.getData(SappyGravity.INVERTED_GRAVITY)) {
            return new Vec3(-vec.x, -vec.y, vec.z);
        } else {
            return vec;
        }
    }

//    @WrapOperation(
//        method = "tickHeadTurn",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/util/Mth;wrapDegrees(F)F",
//            ordinal = 0
//        )
//    )
//    private float modifyRelativeBodyAngle(float value, Operation<Float> original) {
//        value = original.call(value);
//        if (getData(SappyGravity.INVERTED_GRAVITY)) {
//            return -value;
//        } else {
//            return value;
//        }
//    }

//    @ModifyVariable(
//        method = "tickHeadTurn",
//        at = @At(
//            value = "STORE",
//            ordinal = 0
//        )
//    )
//    private float modifyRelativeBodyAngle(float value) {
//        if (getData(SappyGravity.INVERTED_GRAVITY)) {
//            return -value;
//        } else {
//            return value;
//        }
//    }
}
