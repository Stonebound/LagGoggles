package cf.terminator.laggoggles.client.gui.buttons;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;

import static cf.terminator.laggoggles.client.gui.GuiProfile.getSecondsLeftForMessage;
import static cf.terminator.laggoggles.packet.SPacketServerData.PERMISSION;

public class DownloadButton extends Button {

    private ResourceLocation DOWNLOAD_TEXTURE = new ResourceLocation(Main.MODID_LOWER, "download.png");
    private final Screen parent;

    public DownloadButton(Screen parent, int buttonId, int x, int y) {
        super(x, y, 20, 20, "", Button::onPress);
        this.parent = parent;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partial) {
        super.render(mouseX, mouseY, partial);
        Minecraft.getInstance().getTextureManager().bindTexture(DOWNLOAD_TEXTURE);
        blit(x+3,y+3,0,0,14,14,14,14);
        if (isHovered){
            ArrayList<String> hover = new ArrayList<>();
            hover.add("Download the latest available");
            hover.add("world result from the server.");
            if(PERMISSION != Perms.Permission.FULL) {
                hover.add("");
                hover.add("Because you're not opped, the results");
                hover.add("will be trimmed to your surroundings");

                if(getSecondsLeftForMessage() >= 0){
                    hover.add("");
                    hover.add(TextFormatting.GRAY + "Remember: There's a cooldown on this, you");
                    hover.add(TextFormatting.GRAY + "may have to wait before you can use it again.");
                }
            }

            GuiUtils.drawHoveringText(hover, mouseX, mouseY, parent.width, parent.height, -1, Minecraft.getInstance().fontRenderer);

        }
    }
}
