package cf.terminator.laggoggles.mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mixin(value = World.class, priority = 1001)
public abstract class MixinWorld {

    private Long LAGGOGGLES_START = null;

    @Inject(
            method = "func_217391_K()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/ITickableTileEntity;tick()V",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void beforeTick(CallbackInfo ci, Iterator iterator, TileEntity tileentity, BlockPos blockpos){
        LAGGOGGLES_START = System.nanoTime();
    }

    @Inject(
            method = "func_217391_K()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/ITickableTileEntity;tick()V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void afterTick(CallbackInfo ci, Iterator iterator, TileEntity tileentity, BlockPos pos) {
        if (PROFILE_ENABLED.get() && LAGGOGGLES_START != null) {
            timingManager.addBlockTime(tileentity.getWorld().getDimension().getType().getId(), pos, System.nanoTime() - LAGGOGGLES_START);
        }
    }
}
