package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.profiler.TimingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ThreadChecker {

    public static TimingManager.EventTimings.ThreadType getThreadType(){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null){
            /* No server at all. Multiplayer... probably. */
            if(Minecraft.getInstance().isOnExecutionThread()){
                return TimingManager.EventTimings.ThreadType.CLIENT;
            }
        }else{
            if (server.isDedicatedServer() == true) {
                /* Dedicated server */
                if (server.isOnExecutionThread()) {
                    return TimingManager.EventTimings.ThreadType.SERVER;
                }
            } else {
                /* Not a dedicated server, we have both the client and server classes. */
                if (server.isOnExecutionThread()) {
                    return TimingManager.EventTimings.ThreadType.SERVER;
                } else if (Minecraft.getInstance().isOnExecutionThread()) {
                    return TimingManager.EventTimings.ThreadType.CLIENT;
                }
            }
        }
        return TimingManager.EventTimings.ThreadType.ASYNC;
    }
}
