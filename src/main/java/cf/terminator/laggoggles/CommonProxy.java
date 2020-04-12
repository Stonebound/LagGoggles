package cf.terminator.laggoggles;

import cf.terminator.laggoggles.packet.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum CommonProxy {
    SCAN_RESULT(SPacketScanResult.class, SPacketScanResult::new),
    PROFILE_STATUS(SPacketProfileStatus.class, SPacketProfileStatus::new),
    SERVER_DATA(SPacketServerData.class, SPacketServerData::new),
    MESSAGE(SPacketMessage.class, SPacketMessage::new),

    // Client to Server
    REQUEST_ENTITY_TELEPORT(CPacketRequestEntityTeleport.class, CPacketRequestEntityTeleport::new),
    REQUEST_SERVER_DATA(CPacketRequestServerData.class, CPacketRequestServerData::new),
    REQUEST_SCAN(CPacketRequestScan.class, CPacketRequestScan::new),
    REQUEST_TILEENTITY_TELEPORT(CPacketRequestTileEntityTeleport.class, CPacketRequestTileEntityTeleport::new),
    REQUEST_RESULT(CPacketRequestResult.class, CPacketRequestResult::new),

    ;

    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Main.MODID_LOWER, "network");
    public static final String NETWORK_VERSION = new ResourceLocation(Main.MODID_LOWER, "1").toString();
    public static SimpleChannel channel;
    private LoadedPacket<?> packet;

    private <T extends SimplePacketBase> CommonProxy(Class<T> type, Function<PacketBuffer, T> factory) {
        packet = new LoadedPacket<>(type, factory);
    }

    public static void registerPackets() {
        channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME).serverAcceptedVersions(s -> true)
                .clientAcceptedVersions(s -> true).networkProtocolVersion(() -> NETWORK_VERSION).simpleChannel();
        for (CommonProxy packet : values())
            packet.packet.register();

    }

    private static class LoadedPacket<T extends SimplePacketBase> {
        private static int index = 0;
        BiConsumer<T, PacketBuffer> encoder;
        Function<PacketBuffer, T> decoder;
        BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        Class<T> type;

        private LoadedPacket(Class<T> type, Function<PacketBuffer, T> factory) {
            encoder = T::write;
            decoder = factory;
            handler = T::handle;
            this.type = type;
        }

        private void register() {
            channel.messageBuilder(type, index++).encoder(encoder).decoder(decoder).consumer(handler).add();
        }
    }

//    public static void sendTo(IMessage msg, ServerPlayerEntity player){
//        channel.sendTo(msg, player);
//    }
//
//    public static void sendTo(ProfileResult result, ServerPlayerEntity player){
//        List<SPacketScanResult> packets = Perms.getResultFor(player, result).createPackets(player);
//        new RunInServerThread(new Runnable() {
//            @Override
//            public void run() {
//                for (SPacketScanResult result : packets){
//                    sendTo(result, player);
//                }
//            }
//        });
//    }


}