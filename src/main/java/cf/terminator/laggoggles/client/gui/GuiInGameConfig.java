//package cf.terminator.laggoggles.client.gui;
//
//
//import cf.terminator.laggoggles.Main;
//import cf.terminator.laggoggles.config.ClientConfig;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraftforge.common.config.ConfigElement;
//import net.minecraftforge.common.config.Configuration;
//import net.minecraftforge.fml.client.config.GuiConfig;
//import net.minecraftforge.fml.client.config.IConfigElement;
//
//import java.util.ArrayList;
//
//public class GuiInGameConfig extends GuiConfig {
//
//    public GuiInGameConfig(Screen parent) {
//        super(parent, new ClientConfigList(), Main.MODID_LOWER, false, false, Main.MODID + " configuration", "Hover with the mouse over a variable to see a description");
//    }
//
//    @Override
//    public void onGuiClosed(){
//        super.onGuiClosed();
//    }
//
//    public static class ClientConfigList extends ArrayList<IConfigElement>{
//
//        public ClientConfigList() {
//            Configuration config = ClientConfig.ConfigurationHolder.getConfiguration();
//            this.addAll(new ConfigElement(config.getCategory("general")).getChildElements());
//        }
//    }
//}