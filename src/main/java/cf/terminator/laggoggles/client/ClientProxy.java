package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.KeyHandler;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.packet.CPacketRequestServerData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import static cf.terminator.laggoggles.packet.SPacketServerData.RECEIVED_RESULT;
import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class ClientProxy {
    public static void addListeners(IEventBus modEventBus) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientProxy::clientSetup);
        });
    }

    public static void clientSetup(FMLClientSetupEvent e){
        ClientRegistry.registerKeyBinding(new KeyHandler("Profile GUI", GLFW.GLFW_KEY_INSERT, Main.MODID, new KeyHandler.CallBack() {
            @Override
            public void onPress() {
                Minecraft.getInstance().displayGuiScreen(new GuiProfile());
            }
        }));

        MinecraftForge.EVENT_BUS.register(new Object(){
            @SubscribeEvent
            public void onLogin(ClientConnectedToServerEvent e){
                RECEIVED_RESULT = false;
                LagOverlayGui.destroy();
                LAST_PROFILE_RESULT.set(null);
                new ClientLoginAction().activate();
            }
        });
    }

    private class ClientLoginAction {

        int count = 0;

        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent e){
            if(RECEIVED_RESULT == true){
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            if(e.phase != TickEvent.Phase.START){
                return;
            }
            if(count++ % 5 == 0){
                CommonProxy.channel.sendToServer(new CPacketRequestServerData());
            }
        }

        public void activate(){
            MinecraftForge.EVENT_BUS.register(this);
        }

    }
}
