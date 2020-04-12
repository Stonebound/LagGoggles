package cf.terminator.laggoggles.packet;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;
import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.config.Config;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.config.ServerConfig;
import cf.terminator.laggoggles.util.Perms;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class CPacketRequestScan extends SimplePacketBase {
    private static HashMap<UUID, Long> COOLDOWN = new HashMap<>();
    public CPacketRequestScan(){
        length = 5;
    }

    public int length;

    public CPacketRequestScan(ByteBuf buf) {
        length = buf.readInt();
    }


    @Override public void write(PacketBuffer buf) {
        buf.writeInt(length);
    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            final ServerPlayerEntity requestee = context.get().getSender();
            Perms.Permission requesteePerms = Perms.getPermission(requestee);

            if(requesteePerms.ordinal() < Perms.Permission.START.ordinal()){
                Main.LOGGER.info(requestee.getName() + " Tried to start the profiler, but was denied to do so!");
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> requestee), new SPacketMessage("No permission"));
                return;
            }

            if(requesteePerms != Perms.Permission.FULL && length > Config.SERVER.NON_OPS_MAX_PROFILE_TIME.get()){
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> requestee),
                        new SPacketMessage("Profile time is too long! You can profile up to " + Config.SERVER.NON_OPS_MAX_PROFILE_TIME.get() + " seconds"
                                + "."));
                return;
            }

            if(PROFILE_ENABLED.get() == true){
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> requestee), new SPacketMessage("Profiler is already running"));
                return;
            }

            long secondsLeft = secondsLeft(requestee.getGameProfile().getId());
            if(secondsLeft > 0 && requesteePerms != Perms.Permission.FULL){
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> requestee), new SPacketMessage("Please wait " + secondsLeft + " seconds."));
                return;
            }

            /* Start profiler */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Profiler.runProfiler(length, ScanType.WORLD, requestee.getCommandSource());

                    /* Send status to users */
                    SPacketProfileStatus status2 = new SPacketProfileStatus(false, length, requestee.getName().toString());
                    for(ServerPlayerEntity user : Perms.getLagGogglesUsers()) {
                        CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> user), status2);
                    }
                    CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> requestee), LAST_PROFILE_RESULT.get());
                    for(ServerPlayerEntity user : Perms.getLagGogglesUsers()) {
                        CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> user), new SPacketServerData(user));
                    }
                }
            }).start();
        });
        context.get().setPacketHandled(true);

    }

    public static long secondsLeft(UUID uuid){
        long lastRequest = COOLDOWN.getOrDefault(uuid, 0L);
        long secondsLeft = Config.SERVER.NON_OPS_PROFILE_COOL_DOWN_SECONDS.get() - ((System.currentTimeMillis() - lastRequest)/1000);
        if(secondsLeft <= 0){
            COOLDOWN.put(uuid, System.currentTimeMillis());
        }
        return secondsLeft;
    }
}
