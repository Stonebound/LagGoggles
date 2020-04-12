package cf.terminator.laggoggles.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.concurrent.Executor;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mixin(value = ServerWorld.class, priority = 1001)
public abstract class MixinWorldServerForge extends World {

    protected MixinWorldServerForge(
            MinecraftServer p_i50703_1_, Executor p_i50703_2_, SaveHandler p_i50703_3_, WorldInfo p_i50703_4_, DimensionType p_i50703_5_, IProfiler p_i50703_6_, IChunkStatusListener p_i50703_7_) {
        super(p_i50703_4_, p_i50703_5_, (p_217442_4_, p_217442_5_) -> {
            return new ServerChunkProvider((ServerWorld)p_217442_4_, p_i50703_3_.getWorldDirectory(), p_i50703_3_.getFixer(), p_i50703_3_.getStructureTemplateManager(), p_i50703_2_, p_217442_4_.getWorldType().createChunkGenerator(p_217442_4_), p_i50703_1_.getPlayerList().getViewDistance(), p_i50703_7_, () -> {
                return p_i50703_1_.getWorld(DimensionType.OVERWORLD).getSavedData();
            });
        }, p_i50703_6_, false);
    }

    private Long LAGGOGGLES_START_TICK = null;
    private Long LAGGOGGLES_START_RANDOM = null;

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/ServerTickList;tick()V",
                     shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void beforeUpdate(boolean bool, CallbackInfoReturnable<Boolean> ci, int integer, Iterator iterator, NextTickListEntry nextTickListEntry, int integer2, BlockState state){
        LAGGOGGLES_START_TICK = System.nanoTime();
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/ServerTickList;tick()V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void afterUpdate(boolean bool, CallbackInfoReturnable<Boolean> ci, int integer, Iterator iterator, NextTickListEntry nextTickListEntry, int integer2, BlockState state){
        if (PROFILE_ENABLED.get() && LAGGOGGLES_START_TICK != null) {
            timingManager.addBlockTime(dimension.getType().getId(), nextTickListEntry.position, System.nanoTime() - LAGGOGGLES_START_TICK);
        }
    }


    @Inject(method = "func_217441_a",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V",
                     shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void beforeUpdateBlocks(CallbackInfo ci, int int1, boolean bool1, boolean bool2, Iterator iterator, Chunk chunk, int int2, int int3, ChunkSection[] storage, int int4, int int5, ChunkSection storage2, int int6, int int7, int int8, int int9, int int10, BlockState state, Block block){
        LAGGOGGLES_START_RANDOM = System.nanoTime();
    }

    @Inject(method = "func_217441_a",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void afterUpdateBlocks(CallbackInfo ci, int int1, boolean bool1, boolean bool2, Iterator iterator, Chunk chunk, int j, int k, ChunkSection[] storage, int int4, int int5, ChunkSection extendedblockstorage, int int6, int int7, int k1, int l1, int i2, BlockState state, Block block){
        if (PROFILE_ENABLED.get() && LAGGOGGLES_START_RANDOM != null) {
            timingManager.addBlockTime(dimension.getType().getId(), new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), System.nanoTime() - LAGGOGGLES_START_RANDOM);
        }
    }

}