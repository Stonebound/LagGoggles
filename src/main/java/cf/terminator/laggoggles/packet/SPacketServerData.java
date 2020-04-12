package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.config.ServerConfig;
import cf.terminator.laggoggles.util.Perms;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SPacketServerData extends SimplePacketBase {
    public static Perms.Permission PERMISSION = Perms.Permission.NONE;
    public static boolean SERVER_HAS_RESULT = false;
    public static int MAX_SECONDS = Integer.MAX_VALUE;
    public static boolean RECEIVED_RESULT = false;
    public static boolean NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS = false;

    public boolean hasResult = false;
    public Perms.Permission permission;
    public int maxProfileTime = ServerConfig.NON_OPS_MAX_PROFILE_TIME;
    public boolean canSeeEventSubScribers = ServerConfig.ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS;

    public SPacketServerData(){}
    public SPacketServerData(ServerPlayerEntity player){
        hasResult = true;
        permission = Perms.getPermission(player);
    }

    public SPacketServerData(ByteBuf byteBuf) {
        hasResult = byteBuf.readBoolean();
        permission = Perms.Permission.values()[byteBuf.readInt()];
        maxProfileTime = byteBuf.readInt();
        canSeeEventSubScribers = byteBuf.readBoolean();
    }


    @Override
    public void write(PacketBuffer buf) {
        buf.writeBoolean(hasResult);
        buf.writeInt(permission.ordinal());
        buf.writeInt(maxProfileTime);
        buf.writeBoolean(canSeeEventSubScribers);
    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            SERVER_HAS_RESULT = hasResult;
            PERMISSION = permission;
            MAX_SECONDS = PERMISSION == Perms.Permission.FULL ? Integer.MAX_VALUE : maxProfileTime;
            RECEIVED_RESULT = true;
            NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS = canSeeEventSubScribers;
            GuiProfile.update();
        });
        context.get().setPacketHandled(true);

    }
}
