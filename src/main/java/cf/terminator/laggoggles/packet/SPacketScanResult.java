package cf.terminator.laggoggles.packet;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

import cf.terminator.laggoggles.api.event.LagGogglesEvent;
import cf.terminator.laggoggles.config.ClientConfig;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Calculations;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SPacketScanResult extends SimplePacketBase {

    public ArrayList<ObjectData> DATA = new ArrayList<>();
    public boolean hasMore = false;
    public long startTime;
    public long endTime;
    public long totalTime;
    public long tickCount;
    public Dist side;
    public ScanType type;
    public long totalFrames = 0;

    public SPacketScanResult(PacketBuffer buf) {
        tickCount = buf.readLong();
        hasMore = buf.readBoolean();
        endTime = buf.readLong();
        startTime = buf.readLong();
        totalTime = buf.readLong();
        totalFrames = buf.readLong();
        side = Dist.values()[buf.readInt()];
        type = ScanType.values()[buf.readInt()];

        int size = buf.readInt();
        for(int i=0; i<size; i++){
            ObjectData data = new ObjectData(buf);
            DATA.add(data);
        }
    }

    public SPacketScanResult(long endTime, long startTime, long tickCount, long totalTime, Dist side, ScanType type, long totalFrames, ArrayList<ObjectData> object_data) {
        this.endTime = endTime;
        this.startTime = startTime;
        this.tickCount = tickCount;
        this.totalTime = totalTime;
        this.side = side;
        this.type = type;
        this.totalFrames = totalFrames;
        this.DATA.addAll(object_data);
    }

    public SPacketScanResult(long endTime, long startTime, long tickCount, long totalTime, Dist side, ScanType type, long totalFrames,
            ArrayList<ObjectData> object_data, boolean hasMore) {
        this.endTime = endTime;
        this.startTime = startTime;
        this.tickCount = tickCount;
        this.totalTime = totalTime;
        this.side = side;
        this.type = type;
        this.totalFrames = totalFrames;
        this.DATA.addAll(object_data);
        this.hasMore = hasMore;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeLong(tickCount);
        buf.writeBoolean(hasMore);
        buf.writeLong(endTime);
        buf.writeLong(startTime);
        buf.writeLong(totalTime);
        buf.writeLong(totalFrames);
        buf.writeInt(side.ordinal());
        buf.writeInt(type.ordinal());

        buf.writeInt(DATA.size());
        for(ObjectData data : DATA){
            data.write(buf);
        }
    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ArrayList<ObjectData> builder = new ArrayList<>();
            final long tickCount = tickCount > 0 ? tickCount : 1;
            for(ObjectData objectData : DATA){
                if(Calculations.muPerTickCustomTotals(objectData.getValue(ObjectData.Entry.NANOS), tickCount) >= ClientConfig.MINIMUM_AMOUNT_OF_MICROSECONDS_THRESHOLD){
                    builder.add(objectData);
                }
            }
            if (!hasMore){
                ProfileResult result = new ProfileResult(startTime, endTime, tickCount, side, type);
                result.addAll(builder);
                result.lock();
                builder = new ArrayList<>();
                LAST_PROFILE_RESULT.set(result);
                LagOverlayGui.create(result);
                LagOverlayGui.show();
                GuiProfile.update();
                MinecraftForge.EVENT_BUS.post(new LagGogglesEvent.ReceivedFromServer(result));
            }
        });
        context.get().setPacketHandled(true);
    }
}