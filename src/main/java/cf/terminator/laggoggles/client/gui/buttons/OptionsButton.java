package cf.terminator.laggoggles.client.gui.buttons;

import net.minecraft.client.gui.widget.button.Button;

public class OptionsButton extends Button {

    public OptionsButton(int buttonId, int x, int y) {
        super(x, y, 90, 20, "Options", Button::onPress);
    }

}
