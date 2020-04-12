package cf.terminator.laggoggles.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mixin(value = Entity.class, priority = 1001)
public abstract class MixinEntity {

    @Shadow
    protected UUID entityUniqueID;

    @Shadow
    public DimensionType dimension;

    private Long LAGGOGGLES_START = null;

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void onEntityUpdateHEAD(CallbackInfo info){
        LAGGOGGLES_START = System.nanoTime();
    }

    @Inject(method = "baseTick", at = @At("RETURN"))
    public void onEntityUpdateRETURN(CallbackInfo info){
        if(PROFILE_ENABLED.get() && LAGGOGGLES_START != null){
            timingManager.addEntityTime(dimension.getId(), entityUniqueID, System.nanoTime() - LAGGOGGLES_START);
        }
    }





}
