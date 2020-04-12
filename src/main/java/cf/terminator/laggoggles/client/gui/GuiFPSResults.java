package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.profiler.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class GuiFPSResults extends Screen {

    private final ProfileResult result;
    private final FontRenderer FONTRENDERER;

    private GuiEntityTypes guiEntityTypes;
    private GuiSingleEntities guiSingleEntities;
    private GuiEventTypes guiEventTypes;

    public GuiFPSResults(ProfileResult result){
        super(new StringTextComponent("FPS Result"));
        this.result = result;
        FONTRENDERER = Minecraft.getInstance().fontRenderer;
    }

    @Override
    public void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        /*                                            width  , height              , top                   , bottom         , left      , screenWidth, screenHeight, ProfileResult*/
        guiSingleEntities = new GuiSingleEntities(mc, width/2, height - 25         , 45                    , height         ,  0        , width      , height      , result);
        guiEntityTypes    = new GuiEntityTypes(   mc, width/2, (height - 25)/2     , 45                    , (height - 25)/2,  width/2  , width      , height      , result);
        guiEventTypes     = new GuiEventTypes(    mc, width/2, (height - 25)/2 - 12, ((height - 25)/2) + 12, height         ,  width/2  , width      , height      , result);
    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        super.renderBackground(0);
        super.render(mouseX, mouseY, partialTicks);
        guiSingleEntities.render(mouseX, mouseY, partialTicks);
        guiEntityTypes.render(mouseX, mouseY, partialTicks);
        guiEventTypes.render(mouseX, mouseY, partialTicks);
        drawString(Main.MODID + ": profile data for FPS scan results", 5, 5, 0xFFFFFF);
        drawString("Times are presented in nanoseconds per frame.", 5, 15, 0xCCCCCC);
        drawString("Single entities", 5, 35, 0xFFFFFF);
        drawString(" (Doubleclick to teleport)", 5 + FONTRENDERER.getStringWidth("Single entities"), 35, 0x666666);
        drawString("Entities by type", width/2 + 5, 35, 0xFFFFFF);
        drawString("Event subscribers", width/2 + 5, ((height - 25)/2) + 2, 0xFFFFFF);
    }

    private void drawString(String text, int x, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, (float) x, (float)y, color);
    }
}
