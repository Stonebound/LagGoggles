package cf.terminator.laggoggles.profiler;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.atomic.AtomicLong;

public class TickCounter {

    public static AtomicLong ticks = new AtomicLong(0L);

    @SubscribeEvent
    public void addTick(TickEvent.ServerTickEvent e) {
        if(e.phase == TickEvent.Phase.START) {
            ticks.incrementAndGet();
        }
    }
}
