package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SPacketProfileStatus extends SimplePacketBase {

    public boolean isProfiling = true;
    public String issuedBy = "Unknown";
    public int length = 0;

    public SPacketProfileStatus(){}
    public SPacketProfileStatus(boolean isProfiling, int length, String issuedBy){
        this.isProfiling = isProfiling;
        this.length = length;
        this.issuedBy = issuedBy;
    }

    public SPacketProfileStatus(ByteBuf buf){
        isProfiling = buf.readBoolean();
        length = buf.readInt();
        issuedBy = ByteBufUtil.readUTF8String(buf);
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBoolean(isProfiling);
        buf.writeInt(length);
        ByteBufUtil.writeUTF8String(buf, issuedBy);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            GuiProfile.PROFILING_PLAYER = issuedBy;
            if(isProfiling == true) {
                GuiProfile.PROFILE_END_TIME = System.currentTimeMillis() + (length * 1000);
            }else{
                GuiProfile.PROFILE_END_TIME = System.currentTimeMillis();
            }
            GuiProfile.update();
        });
        context.get().setPacketHandled(true);
    }
}
