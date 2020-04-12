package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.event.LagGogglesEvent;
import cf.terminator.laggoggles.client.FPSCounter;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.packet.SPacketProfileStatus;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.RunInClientThread;
import cf.terminator.laggoggles.util.RunInServerThread;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static cf.terminator.laggoggles.util.Graphical.formatClassName;

public class ProfileManager {

    public static TimingManager timingManager = new TimingManager();
    public static final AtomicBoolean PROFILE_ENABLED = new AtomicBoolean(false);
    public static final AtomicReference<ProfileResult> LAST_PROFILE_RESULT = new AtomicReference<>();
    private static final Object LOCK = new Object();
    private static final FPSCounter FPS_COUNTER = new FPSCounter();

    public static ProfileResult runProfiler(int seconds, ScanType type, CommandSource issuer) throws IllegalStateException{
        try {
            if(PROFILE_ENABLED.get()){
                throw new IllegalStateException("Can't start profiler when it's already running!");
            }

            /* Send status to users */
            SPacketProfileStatus status = new SPacketProfileStatus(true, seconds, issuer.getName());

            new RunInServerThread(new Runnable() {
                @Override
                public void run() {
                    for(ServerPlayerEntity user : Perms.getLagGogglesUsers()) {
                        CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> user), status);
                    }
                }
            });

            issuer.sendFeedback(new StringTextComponent(TextFormatting.GRAY + Main.MODID + TextFormatting.WHITE + ": Profiler started for " + seconds + " seconds."), true);
            Main.LOGGER.info(Main.MODID + " profiler started by " + issuer.getName() + " (" + seconds + " seconds)");

            long start = System.nanoTime();
            TickCounter.ticks.set(0L);
            timingManager = new TimingManager();
            if(FMLEnvironment.dist.isClient()) {
                FPS_COUNTER.start();
            }
            PROFILE_ENABLED.set(true);
            Thread.sleep(seconds * 1000);
            PROFILE_ENABLED.set(false);
            long frames = FPS_COUNTER.stop();

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try{
                        ArrayList<Entity> ignoredEntities = new ArrayList<>();
                        ArrayList<TileEntity> ignoredTileEntities = new ArrayList<>();
                        ArrayList<BlockPos> ignoredBlocks = new ArrayList<>();

                        Main.LOGGER.info("Processing results synchronously...");
                        ProfileResult result = new ProfileResult(start, System.nanoTime(), TickCounter.ticks.get(), FMLEnvironment.dist, type);
                        if(FMLEnvironment.dist.isClient()) {
                            result.setFrames(frames);
                        }

                        for(Map.Entry<Integer, TimingManager.WorldData> entry : timingManager.getTimings().entrySet()){
                            int worldID = entry.getKey();
                            ServerWorld world =
                                    DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), DimensionType.getById(worldID), false, false); //
                            if(world == null){
                                continue;
                            }
                            for(Map.Entry<UUID, Long> entityTimes : entry.getValue().getEntityTimes().entrySet()){
                                Entity e = world.getEntityByUuid(entityTimes.getKey());
                                if(e == null){
                                    continue;
                                }
                                try {
                                    result.addData(new ObjectData(
                                            worldID,
                                            e.getName(),
                                            formatClassName(e.getClass().toString()),
                                            e.getUniqueID(),
                                            entityTimes.getValue(),
                                            ObjectData.Type.ENTITY)
                                    );
                                }catch (Throwable t){
                                    ignoredEntities.add(e);
                                }
                            }
                            for(Map.Entry<BlockPos, Long> tileEntityTimes : entry.getValue().getBlockTimes().entrySet()){
                                if(world.isBlockLoaded(tileEntityTimes.getKey()) == false){
                                    continue;
                                }
                                TileEntity e = world.getTileEntity(tileEntityTimes.getKey());
                                if(e != null) {
                                    try {
                                        String name = e.getType().getRegistryName().toString();

                                        result.addData(new ObjectData(
                                                worldID,
                                                name,
                                                formatClassName(e.getClass().toString()),
                                                e.getPos(),
                                                tileEntityTimes.getValue(),
                                                ObjectData.Type.TILE_ENTITY)
                                        );
                                    }catch (Throwable t){
                                        ignoredTileEntities.add(e);
                                    }
                                }else{
                                    /* The block is not a tile entity, get the actual block. */
                                    try {
                                        BlockState state = world.getBlockState(tileEntityTimes.getKey());
                                        String name = state.getBlock().getRegistryName().toString();
                                        result.addData(new ObjectData(
                                                worldID,
                                                name,
                                                formatClassName(state.getBlock().getClass().toString()),
                                                tileEntityTimes.getKey(),
                                                tileEntityTimes.getValue(),
                                                ObjectData.Type.BLOCK));
                                    }catch (Throwable t){
                                        ignoredBlocks.add(tileEntityTimes.getKey());
                                    }
                                }
                            }
                        }
                        for(Map.Entry<TimingManager.EventTimings, AtomicLong> entry : timingManager.getEventTimings().entrySet()){
                            result.addData(new ObjectData(entry.getKey(), entry.getValue().get()));
                        }
                        if(result.getSide().isClient()) {
                            insertGuiData(result, timingManager);
                        }
                        result.lock();
                        LAST_PROFILE_RESULT.set(result);
                        synchronized (LOCK){
                            LOCK.notifyAll();
                        }
                        if(ignoredBlocks.size() + ignoredEntities.size() + ignoredTileEntities.size() > 0) {
                            Main.LOGGER.info("Ignored some tracked elements:");
                            Main.LOGGER.info("Entities: " + ignoredEntities);
                            Main.LOGGER.info("Tile entities: " + ignoredTileEntities);
                            Main.LOGGER.info("Blocks in locations: " + ignoredBlocks);
                        }
                    } catch (Throwable e) {
                        Main.LOGGER.error("Woa! Something went wrong while processing results! Please contact Terminator_NL and submit the following error in an issue at github!");
                        e.printStackTrace();
                    }
                }
            };
            Dist side = FMLEnvironment.dist;
            if(side.isDedicatedServer()){
                new RunInServerThread(task);
            }else if(side.isClient()){
                new RunInClientThread(task);
            }else{
                Main.LOGGER.error("LagGoggles did something amazing. I have no clue how this works, but here's a stacktrace, please submit an issue at github with the stacktrace below!");
                Thread.dumpStack();
            }
            synchronized (LOCK) {
                LOCK.wait();
            }
            MinecraftForge.EVENT_BUS.post(new LagGogglesEvent.LocalResult(LAST_PROFILE_RESULT.get()));
            Main.LOGGER.info("Profiling complete.");
            issuer.sendFeedback(new StringTextComponent(TextFormatting.GRAY + Main.MODID + TextFormatting.WHITE + ": Profiling complete."), true);
            return LAST_PROFILE_RESULT.get();
        } catch (Throwable e) {
            Main.LOGGER.error("Woa! Something went wrong while processing results! Please contact Terminator_NL and submit the following error in an issue at github!");
            e.printStackTrace();
            return null;
        }
    }

    public static void insertGuiData(ProfileResult result, TimingManager timings) {
        TreeMap<UUID, Long> entityTimes = timings.getGuiEntityTimings();
        for (Entity e : Minecraft.getInstance().world.getAllEntities()) {
            Long time = entityTimes.get(e.getUniqueID());
            if (time == null) {
                continue;
            }
            result.addData(new ObjectData(
                    e.world.getDimension().getType().getId(),
                    e.getName(),
                    formatClassName(e.getClass().toString()),
                    e.getUniqueID(),
                    time,
                    ObjectData.Type.GUI_ENTITY)
            );
        }

        TreeMap<BlockPos, Long> blockTimes = timings.getGuiBlockTimings();
        ClientWorld world = Minecraft.getInstance().world;
        for (Map.Entry<BlockPos, Long> e: blockTimes.entrySet()) {
            Long time = e.getValue();
            TileEntity entity = world.getTileEntity(e.getKey());
            if(entity != null) {
                String name = entity.getType().getRegistryName().toString();
                result.addData(new ObjectData(
                        entity.getWorld().getDimension().getType().getId(),
                        name,
                        formatClassName(entity.getClass().toString()),
                        entity.getPos(),
                        time,
                        ObjectData.Type.GUI_BLOCK)
                );
            }else{
                /* The block is not a tile entity, get the actual block. */
                BlockState state = world.getBlockState(e.getKey());
                String name = state.getBlock().getNameTextComponent().toString();
                result.addData(new ObjectData(
                        world.getDimension().getType().getId(),
                        name,
                        formatClassName(state.getBlock().getClass().toString()),
                        e.getKey(),
                        time,
                        ObjectData.Type.GUI_BLOCK));
            }
        }
    }

}
