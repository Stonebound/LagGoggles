package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class SPacketMessage extends SimplePacketBase {

    public String message;
    public int seconds = 3;

    public SPacketMessage(){}
    public SPacketMessage(String msg){
        message = msg;
    }

    public SPacketMessage(ByteBuf buf) {
        message = ByteBufUtil.readUTF8String(buf);
        seconds = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) {
        ByteBufUtil.writeUTF8String(buf, message);
        buf.writeInt(seconds);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            GuiProfile.MESSAGE = message;
            GuiProfile.MESSAGE_END_TIME = System.currentTimeMillis() + (seconds * 1000);
            GuiProfile.update();
            Main.LOGGER.info("message received from server: " + message);
            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(TextFormatting.RED + message));
            CommonProxy.channel.send(PacketDistributor.PLAYER.with(() -> context.get().getSender()), new CPacketRequestServerData());
        });
        context.get().setPacketHandled(true);
    }
}
