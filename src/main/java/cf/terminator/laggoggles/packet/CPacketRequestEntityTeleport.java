package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.RunInServerThread;
import cf.terminator.laggoggles.util.Teleport;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class CPacketRequestEntityTeleport extends SimplePacketBase {

    public UUID uuid;
    public CPacketRequestEntityTeleport(){}
    public CPacketRequestEntityTeleport(UUID uuid){
        this.uuid = uuid;
    }


    public CPacketRequestEntityTeleport(ByteBuf buf){
        uuid = new UUID(buf.readLong(), buf.readLong());
    }

    @Override public void write(PacketBuffer buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(!Perms.hasPermission(player, Perms.Permission.FULL)){
                Main.LOGGER.info(player.getName() + " tried to teleport, but was denied to do so!");
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), new SPacketMessage("No permission"));
                return;
            }
            new RunInServerThread(new Runnable() {
                @Override
                public void run() {
                    Entity e = ServerLifecycleHooks.getCurrentServer().getEntityFromUuid(uuid);
                    if(e == null){
                        player.sendMessage(new StringTextComponent(TextFormatting.RED + "Woops! This entity no longer exists!"));
                        return;
                    }
                    Teleport.teleportPlayer(player, e.dimension, e.posX, e.posY, e.posZ);
                }
            });
        });
        context.get().setPacketHandled(true);

    }
}
