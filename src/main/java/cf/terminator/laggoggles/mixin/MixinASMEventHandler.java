package cf.terminator.laggoggles.mixin;

import net.minecraftforge.eventbus.ASMEventHandler;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ASMEventHandler.class, priority = 1001, remap = false)
public abstract class MixinASMEventHandler implements IEventListener, cf.terminator.laggoggles.util.ASMEventHandler {

    @Shadow
    private ModContainer owner;

    @Override
    public ModContainer getOwner(){
        return owner;
    }

}
