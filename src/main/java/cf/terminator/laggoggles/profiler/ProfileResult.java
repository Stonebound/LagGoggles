package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.client.gui.GuiScanResultsWorld;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProfileResult {

    public static final ProfileResult EMPTY_RESULT = new ProfileResult(0,0,0, Dist.DEDICATED_SERVER, ScanType.EMPTY);

    private final long startTime;
    private long endTime;
    private long totalTime;
    private long tickCount;
    private boolean isLocked = false;
    private Dist side;
    private ScanType type;
    private long totalFrames = 0;
    private List<ObjectData> cachedList = null;

    private final ArrayList<ObjectData> OBJECT_DATA = new ArrayList<>();
    private List<GuiScanResultsWorld.LagSource> lagSources = null;

    public ProfileResult(long startTime, long endTime, long tickCount, Dist side, ScanType type){
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalTime = endTime - startTime;
        this.tickCount = tickCount == 0 ? 1 : tickCount;
        this.side = side;
        this.type = type;
    }

    private ProfileResult(ProfileResult original, boolean copyData){
        this.startTime = original.startTime;
        this.endTime = original.endTime;
        this.totalTime = original.totalTime;
        this.tickCount = original.tickCount;
        this.totalFrames = original.totalFrames;
        this.type = original.type;
        this.side = original.side;
        if(copyData == true) {
            OBJECT_DATA.addAll(original.OBJECT_DATA);
        }
    }

    public void setFrames(long frames){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        totalFrames = frames;
    }

    public void addData(ObjectData data){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        OBJECT_DATA.add(data);
    }

    public boolean removeData(ObjectData data){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        return OBJECT_DATA.remove(data);
    }

    public boolean addAll(Collection<? extends ObjectData> data){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        return OBJECT_DATA.addAll(data);
    }

    /**
     * Clones the dataset. If you wish to modify it, use {@link #addData} and {@link #removeData}
     * @return object data
     */
    public List<ObjectData> getData() {
        if(cachedList == null){
            cachedList = Collections.unmodifiableList(OBJECT_DATA);
        }
        return cachedList;
    }

    public List<GuiScanResultsWorld.LagSource> getLagSources(){
        if(lagSources == null){
            ArrayList<GuiScanResultsWorld.LagSource> tmp = new ArrayList<>();
            for(ObjectData entity : getData()){
                tmp.add(new GuiScanResultsWorld.LagSource(entity.<Long>getValue(ObjectData.Entry.NANOS), entity));
            }
            Collections.sort(tmp);
            lagSources = Collections.unmodifiableList(tmp);
        }
        return lagSources;
    }

    public long getStartTime(){
        return startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public long getTickCount(){
        return tickCount;
    }

    public Dist getSide(){
        return side;
    }

    public ScanType getType() {
        return type;
    }

    public long getTotalFrames(){
        return totalFrames;
    }

    public ProfileResult copy(){
        return new ProfileResult(this, true);
    }

    public ProfileResult copyStatsOnly(){
        return new ProfileResult(this, false);
    }

    public long getTotalTime(){
        return totalTime;
    }

    public double getTPS(){
        double seconds = totalTime / 1000000000D;
        return tickCount/seconds;
    }

    public double getFPS(){
        double seconds = totalTime / 1000000000D;
        return totalFrames/seconds;
    }

    public double getNanoPerFrame(){
        return (double)totalTime/(double)totalFrames;
    }

    public void lock(){
        isLocked = true;
    }

    private SPacketScanResult getPacket(){
        SPacketScanResult result = new SPacketScanResult(this.endTime, this.startTime, this.tickCount, this.totalTime, this.side, this.type,
                this.totalFrames, OBJECT_DATA);
        return result;
    }


    public List<SPacketScanResult> createPackets(ServerPlayerEntity player){
        ArrayList<SPacketScanResult> list = new ArrayList<>();
        ArrayList<ObjectData> data = new ArrayList<>(OBJECT_DATA);
        player.sendMessage(new StringTextComponent(TextFormatting.GRAY + "LagGoggles" + TextFormatting.WHITE + ": Generating the results for you..."));
        long time = System.currentTimeMillis();
        double dataSize = data.size();
        while(data.size() > 0) {
            ArrayList<ObjectData> sub = new ArrayList<>(data.subList(0,Math.min(50,data.size())));
            data.removeAll(sub);
            SPacketScanResult packet = new SPacketScanResult(this.endTime, this.startTime, this.tickCount, this.totalTime, this.side, this.type,
                    this.totalFrames, sub, true);
            list.add(packet);
            if(time + 5000 < System.currentTimeMillis()){
                time = System.currentTimeMillis();
                player.sendMessage(new StringTextComponent(TextFormatting.GRAY + "LagGoggles" + TextFormatting.WHITE + ": result is processing: " + Math.round(100 - (int) ((double) data.size()/dataSize * 100d)) + "%"));
            }
        }
        if(list.size() >= 1) {
            list.get(list.size() - 1).hasMore = false;
        }
        player.sendMessage(new StringTextComponent(TextFormatting.GRAY + "LagGoggles" + TextFormatting.WHITE + ": Done!"));
        return list;
    }

}
