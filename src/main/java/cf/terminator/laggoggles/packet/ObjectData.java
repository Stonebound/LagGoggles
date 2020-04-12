package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.profiler.TimingManager;
import cf.terminator.laggoggles.util.Coder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

import static cf.terminator.laggoggles.util.Graphical.formatClassName;

public class ObjectData extends SimplePacketBase {

    private TreeMap<Entry, Object> data = new TreeMap<>();
    public Type type;

    ObjectData(){}

    public ObjectData(ByteBuf buf) {
        type = Type.values()[buf.readInt()];
        int size = buf.readInt();
        for(int i=0; i<size; i++){
            Entry entry = Entry.values()[buf.readInt()];
            data.put(entry, entry.coder.read(buf));
        }
    }

    @Override public void write(PacketBuffer buf) {
        buf.writeInt(type.ordinal());
        buf.writeInt(data.size());
        for(Map.Entry<Entry, Object> entry : data.entrySet()){
            buf.writeInt(entry.getKey().ordinal());
            entry.getKey().coder.write(entry.getValue(), buf);
        }
    }

    @Override public void handle(Supplier<NetworkEvent.Context> context) {

    }

    public enum Type{
        ENTITY,
        TILE_ENTITY,
        BLOCK,
        EVENT_BUS_LISTENER,

        GUI_ENTITY,
        GUI_BLOCK
    }

    public enum Entry{
        WORLD_ID(Coder.INTEGER),

        ENTITY_NAME(Coder.STRING),
        ENTITY_UUID(Coder.UUID),
        ENTITY_CLASS_NAME(Coder.STRING),

        BLOCK_NAME(Coder.STRING),
        BLOCK_POS_X(Coder.INTEGER),
        BLOCK_POS_Y(Coder.INTEGER),
        BLOCK_POS_Z(Coder.INTEGER),
        BLOCK_CLASS_NAME(Coder.STRING),

        EVENT_BUS_LISTENER(Coder.STRING),
        EVENT_BUS_EVENT_CLASS_NAME(Coder.STRING),
        EVENT_BUS_THREAD_TYPE(Coder.INTEGER),

        NANOS(Coder.LONG);

        public final Coder coder;

        Entry(Coder d){
            this.coder = d;
        }
    }

    public ObjectData(int worldID, ITextComponent name, String className, UUID id, long nanos, Type type_){
        type = type_;
        data.put(Entry.WORLD_ID, worldID);
        data.put(Entry.ENTITY_NAME, name);
        data.put(Entry.ENTITY_CLASS_NAME, className);
        data.put(Entry.ENTITY_UUID, id);
        data.put(Entry.NANOS, nanos);
    }

    public ObjectData(int worldID, String name, String className, BlockPos pos, long nanos, Type type_){
        type = type_;
        data.put(Entry.WORLD_ID, worldID);
        data.put(Entry.BLOCK_NAME, name);
        data.put(Entry.BLOCK_CLASS_NAME, className);
        data.put(Entry.BLOCK_POS_X, pos.getX());
        data.put(Entry.BLOCK_POS_Y, pos.getY());
        data.put(Entry.BLOCK_POS_Z, pos.getZ());
        data.put(Entry.NANOS, nanos);
    }

    public ObjectData(TimingManager.EventTimings eventTimings, long nanos){
        type = Type.EVENT_BUS_LISTENER;
        data.put(Entry.EVENT_BUS_EVENT_CLASS_NAME, formatClassName(eventTimings.eventClass.toString()));
        data.put(Entry.EVENT_BUS_LISTENER, formatClassName(eventTimings.listener));
        data.put(Entry.EVENT_BUS_THREAD_TYPE, eventTimings.threadType.ordinal());
        data.put(Entry.NANOS, nanos);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Entry entry){
        if(data.get(entry) == null){
            throw new IllegalArgumentException("Cant find the entry " + entry + " for " + type);
        }
        return (T) data.get(entry);
    }

    @Override
    public String toString(){
        return type.toString() + ": " + data.toString();
    }
}
