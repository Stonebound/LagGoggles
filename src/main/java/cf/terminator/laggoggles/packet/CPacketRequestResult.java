package cf.terminator.laggoggles.packet;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;
import static cf.terminator.laggoggles.profiler.ScanType.FPS;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.config.Config;
import cf.terminator.laggoggles.config.ServerConfig;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class CPacketRequestResult extends SimplePacketBase {
    private static HashMap<UUID, Long> LAST_RESULT_REQUEST = new HashMap<>();

    public CPacketRequestResult() {

    }
    public CPacketRequestResult(PacketBuffer buffer) {

    }
    @Override public void write(PacketBuffer buffer) {

    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(Perms.getPermission(player).ordinal() < Perms.Permission.GET.ordinal()){
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), new SPacketMessage("No permission"));
                return;
            }
            if(LAST_PROFILE_RESULT.get() == null || LAST_PROFILE_RESULT.get().getType() == FPS){
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), new SPacketMessage("No data available"));
                return;
            }
            if(Perms.getPermission(player).ordinal() < Perms.Permission.FULL.ordinal()){
                long secondsLeft = secondsLeft(player.getGameProfile().getId());
                if(secondsLeft > 0){
                    CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), new SPacketMessage("Please wait " + secondsLeft + " seconds."));
                    return;
                }
            }
            CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), LAST_PROFILE_RESULT.get());

        });
        context.get().setPacketHandled(true);
    }

    public static long secondsLeft(UUID uuid){
        long lastRequest = LAST_RESULT_REQUEST.getOrDefault(uuid, 0L);
        long secondsLeft = Config.SERVER.NON_OPS_REQUEST_LAST_SCAN_DATA_TIMEOUT.get() - ((System.currentTimeMillis() - lastRequest)/1000);
        if(secondsLeft <= 0){
            LAST_RESULT_REQUEST.put(uuid, System.currentTimeMillis());
        }
        return secondsLeft;
    }
}
