package cf.terminator.laggoggles.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class SimplePacketBase {
    public abstract void write(PacketBuffer buffer);
    public abstract void handle(Supplier<NetworkEvent.Context> context);
}
