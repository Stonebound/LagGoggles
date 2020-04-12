package cf.terminator.laggoggles.command;

import static cf.terminator.laggoggles.util.ClickableLink.getLink;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Perms;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class AboutCommand {
    static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("about")
                .requires(cs->cs.hasPermissionLevel(0)) //permission
                .then(Commands.argument("start", StringArgumentType.string())
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(new StringTextComponent(TextFormatting.GRAY + "Running LagGoggles version: " + TextFormatting.GREEN + Main.VERSION), true);
                            ctx.getSource().sendFeedback(getLink("https://minecraft.curseforge.com/projects/laggoggles"), true);
                            ctx.getSource().sendFeedback(new StringTextComponent(""), true);
                            ctx.getSource().sendFeedback(new StringTextComponent(TextFormatting.GRAY + "Available arguments:"), true);
                            ctx.getSource().sendFeedback(new StringTextComponent(TextFormatting.GRAY + "/" + Main.MODID_LOWER + " " +TextFormatting.WHITE +
                                    "start <seconds>"), true);
                            ctx.getSource().sendFeedback(new StringTextComponent(TextFormatting.GRAY + "/" + Main.MODID_LOWER + " " +TextFormatting.WHITE + "dump"), true);
                            return 0;
                        })

                );
    }
}
