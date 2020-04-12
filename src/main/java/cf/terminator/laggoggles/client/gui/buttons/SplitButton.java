package cf.terminator.laggoggles.client.gui.buttons;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import net.minecraft.client.gui.widget.button.Button;

import java.util.List;

import static cf.terminator.laggoggles.client.gui.buttons.SplitButton.State.NORMAL;
import static cf.terminator.laggoggles.client.gui.buttons.SplitButton.State.SPLIT;

public abstract class SplitButton extends Button  {

    State state = NORMAL;
    long lastClick = 0;
    enum State{
        NORMAL,
        SPLIT,
    }

    protected final Button clientButton;
    protected final Button serverButton;

    public SplitButton(int x, int y, int widthIn, int heightIn, String buttonText) {
        super(x, y, widthIn, heightIn, buttonText, Button::onPress);
        clientButton = new Button(x + width/2 + 5, y,width/2 - 5, height, "FPS", onPress);
        serverButton = new Button(x,y,width/2 - 5, height, "World", onPress);
    }

    public void onPress(GuiProfile parent, List<Button> buttonList){
        if(lastClick + 50 > System.currentTimeMillis()){
            return;
        }
        lastClick = System.currentTimeMillis();
        updateButtons();
        if(state == NORMAL) {
            state = SPLIT;
            buttonList.remove(this);
            buttonList.add(clientButton);
            buttonList.add(serverButton);
        }else if(state == SPLIT){
            LagOverlayGui.hide();
            buttonList.add(this);
            buttonList.remove(clientButton);
            buttonList.remove(serverButton);
            if(serverButton.isHovered()){
                onWorldClick(parent);
            }else if(clientButton.isHovered()){
                onFPSClick(parent);
            }
            state = NORMAL;
        }
    }

    public void updateButtons(){};

    public abstract void onWorldClick(GuiProfile parent);
    public abstract void onFPSClick(GuiProfile parent);
}
