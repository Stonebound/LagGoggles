package cf.terminator.laggoggles.mixin;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mixin(EntityRendererManager.class)
public class MixinEntityRendererManager {

    @Shadow @Final public Map<Class<? extends Entity>, EntityRenderer<? extends Entity>> renderers;
    private Long LAGGOGGLES_START = null;

    @Inject(method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V", at = @At("HEAD"))
    public void beforeRender(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci){
        LAGGOGGLES_START = System.nanoTime();
    }

    @Inject(method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V", at = @At("RETURN"))
    public void afterRender(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci){
        if(PROFILE_ENABLED.get()){
            timingManager.addGuiEntityTime(entityIn.getUniqueID(), System.nanoTime() - LAGGOGGLES_START);
        }
    }


}