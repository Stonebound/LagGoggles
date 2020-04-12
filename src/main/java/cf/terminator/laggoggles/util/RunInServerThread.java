package cf.terminator.laggoggles.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RunInServerThread {

    private final Runnable runnable;

    public RunInServerThread(Runnable runnable){
        this.runnable = runnable;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e){
        MinecraftForge.EVENT_BUS.unregister(this);
        runnable.run();
    }
}
