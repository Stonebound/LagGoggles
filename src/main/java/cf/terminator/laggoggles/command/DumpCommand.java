package cf.terminator.laggoggles.command;

import cf.terminator.laggoggles.util.Perms;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class DumpCommand {
    static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("dump")
            .requires(cs->cs.hasPermissionLevel(0)) //permission
            .then(Commands.argument("start", StringArgumentType.string())
                .executes(ctx -> {
                    if(!LagGogglesCommand.hasPerms(ctx.getSource(), Perms.Permission.GET)){
                        throw new CommandException(new StringTextComponent("You don't have permission to do this!"));
                    }
                    LagGogglesCommand.dump(ctx.getSource());
                    return 0;
                })

            );
    }
}
