package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.server.RequestDataHandler;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.RunInServerThread;
import cf.terminator.laggoggles.util.Teleport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class CPacketRequestServerData extends SimplePacketBase {
    public CPacketRequestServerData() {

    }
    public CPacketRequestServerData(PacketBuffer buffer) {

    }

    @Override public void write(PacketBuffer buffer) {

    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(!RequestDataHandler.playersWithLagGoggles.contains(player.getGameProfile().getId())) {
                RequestDataHandler.playersWithLagGoggles.add(player.getGameProfile().getId());
            }
            CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), new SPacketServerData(player));

        });
        context.get().setPacketHandled(true);
    }
}
