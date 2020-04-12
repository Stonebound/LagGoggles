package cf.terminator.laggoggles;

import cf.terminator.laggoggles.client.ClientProxy;
import cf.terminator.laggoggles.command.LagGogglesCommand;
//import cf.terminator.laggoggles.mixinhelper.MixinValidator;
import cf.terminator.laggoggles.config.Config;
import cf.terminator.laggoggles.profiler.TickCounter;
import cf.terminator.laggoggles.server.RequestDataHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MODID_LOWER)
public class Main {
    public static final String MODID = "LagGoggles";
    public static final String MODID_LOWER = "laggoggles";
    public static final String VERSION = "${version}";
    public static Logger LOGGER = LogManager.getLogger();

    public Main() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(Main::init);

        MinecraftForge.EVENT_BUS.addListener(Main::onServerStart);

        ClientProxy.addListeners(modEventBus);
    }

    public static void init(FMLCommonSetupEvent e){
//        MixinValidator.validate();
        CommonProxy.registerPackets();
        Config.registerAll();
    }


    public static void onServerStart(FMLServerStartingEvent e){
        new LagGogglesCommand(e.getCommandDispatcher());
        MinecraftForge.EVENT_BUS.register(new TickCounter());
        MinecraftForge.EVENT_BUS.register(new RequestDataHandler());
    }

}
