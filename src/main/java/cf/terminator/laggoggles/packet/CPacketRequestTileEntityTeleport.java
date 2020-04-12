package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.Teleport;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class CPacketRequestTileEntityTeleport extends SimplePacketBase {

    public int dim;
    public int x;
    public int y;
    public int z;

    public CPacketRequestTileEntityTeleport(){}
    public CPacketRequestTileEntityTeleport(ObjectData data){
        dim = data.getValue(ObjectData.Entry.WORLD_ID);
        x =   data.getValue(ObjectData.Entry.BLOCK_POS_X);
        y =   data.getValue(ObjectData.Entry.BLOCK_POS_Y);
        z =   data.getValue(ObjectData.Entry.BLOCK_POS_Z);
    }

    public CPacketRequestTileEntityTeleport(ByteBuf buf){
        dim = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override public void write(PacketBuffer buf) {
        buf.writeInt(dim);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(Perms.hasPermission(player, Perms.Permission.FULL) == false){
                Main.LOGGER.info(player.getName() + " tried to teleport, but was denied to do so!");
                CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> player), new SPacketMessage("No permission"));
                return;
            }
            Teleport.teleportPlayer(player, DimensionType.getById(dim), x, y, z); // TODO: get dimension by id
        });
        context.get().setPacketHandled(true);

    }
}