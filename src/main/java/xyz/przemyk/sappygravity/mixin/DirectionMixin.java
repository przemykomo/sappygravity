package xyz.przemyk.sappygravity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.przemyk.sappygravity.SappyGravity;

@Mixin(Direction.class)
//Fixes placing blocks with incorrect orientation
public abstract class DirectionMixin {

    @WrapOperation(
        method = "orderedByNearest",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F",
            ordinal = 0
        )
    )
    private static float wrapGetViewXRot(Entity entity, float partialTicks, Operation<Float> original) {
        float rot = original.call(entity, partialTicks);
        return entity.getData(SappyGravity.INVERTED_GRAVITY) ? -rot : rot;
    }

    @WrapOperation(
        method = "orderedByNearest",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F",
            ordinal = 0
        )
    )
    private static float wrapGetViewYRot(Entity entity, float partialTicks, Operation<Float> original) {
        float rot = original.call(entity, partialTicks);
        return entity.getData(SappyGravity.INVERTED_GRAVITY) ? -rot : rot;
    }

    @WrapOperation(
        method = "getFacingAxis",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
        )
    )
    private static float wrapGetViewYRotFacingAxis(Entity entity, float partialTicks, Operation<Float> original) {
        float rot = original.call(entity, partialTicks);
        return entity.getData(SappyGravity.INVERTED_GRAVITY) ? -rot : rot;
    }

    @WrapOperation(
        method = "getFacingAxis",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"
        )
    )
    private static float wrapGetViewXRotFacingAxis(Entity entity, float partialTicks, Operation<Float> original) {
        float rot = original.call(entity, partialTicks);
        return entity.getData(SappyGravity.INVERTED_GRAVITY) ? -rot : rot;
    }
}
