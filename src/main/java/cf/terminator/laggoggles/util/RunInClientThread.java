package cf.terminator.laggoggles.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RunInClientThread {

    private final Runnable runnable;

    public RunInClientThread(Runnable runnable){
        this.runnable = runnable;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e){
        MinecraftForge.EVENT_BUS.unregister(this);
        runnable.run();
    }
}
