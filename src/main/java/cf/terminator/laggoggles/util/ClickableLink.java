package cf.terminator.laggoggles.util;

import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;

public class ClickableLink {

    public static StringTextComponent getLink(String link){
        StringTextComponent text = new StringTextComponent(TextFormatting.BLUE + link);
        Style style = text.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        return text;
    }
}
