package cf.terminator.laggoggles.command;

import static cf.terminator.laggoggles.command.LagGogglesCommand.hasPerms;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.packet.CPacketRequestScan;
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

public class StartCommand {
    static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("start")
                .requires(cs->cs.hasPermissionLevel(0)) //permission
                .then(Commands.argument("start", StringArgumentType.string())
                        .then(Commands.argument("seconds", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    if(!hasPerms(ctx.getSource(), Perms.Permission.START)){
                                        throw new CommandException(new StringTextComponent("You don't have permission to do this!"));
                                    }
                                    final int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                    if(!Profiler.canProfile()){
                                        throw new CommandException(new StringTextComponent("Profiler is already running."));
                                    }
                                    if(ctx.getSource().getEntity() instanceof ServerPlayerEntity && !hasPerms(ctx.getSource(), Perms.Permission.FULL)){
                                        long secondsLeft = CPacketRequestScan.secondsLeft(ctx.getSource().asPlayer().getGameProfile().getId());
                                        if(secondsLeft > 0) {
                                            throw new CommandException(new StringTextComponent("Please wait " + secondsLeft + " seconds."));
                                        }
                                    }
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Profiler.runProfiler(seconds, ScanType.WORLD, ctx.getSource());
                                            ctx.getSource().sendFeedback(new StringTextComponent(
                                                    TextFormatting.GRAY + Main.MODID + TextFormatting.WHITE + ": You can see results using /" + Main.MODID_LOWER +" dump"), true);
                                        }
                                    }).start();
                                    return 0;
                                })
                        )
                );
    }
}
