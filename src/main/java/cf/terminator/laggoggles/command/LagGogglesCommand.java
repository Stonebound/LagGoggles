package cf.terminator.laggoggles.command;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.client.gui.GuiScanResultsWorld;
import cf.terminator.laggoggles.packet.CPacketRequestScan;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Perms;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class LagGogglesCommand {

    public LagGogglesCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal(Main.MODID_LOWER)
                .then(DumpCommand.register())
                .then(StartCommand.register())
        );
    }

    public static boolean hasPerms(CommandSource sender, Perms.Permission permission) throws CommandSyntaxException {
        Entity source = sender.getEntity();
        if (source instanceof ServerPlayerEntity){
            return Perms.hasPermission(sender.asPlayer(), permission);
        } else {
            Main.LOGGER.info("Unknown object is executing a command, assuming it's okay. Object: (" + sender + ") Class: (" + sender.getClass().toString() + ")");
            return true;
        }
    }

    public static void dump(CommandSource sender) throws CommandException, CommandSyntaxException {
        Entity source = sender.getEntity();
        ProfileResult fullResult = Profiler.getLatestResult();
        if(fullResult == null){
            throw new CommandException(new StringTextComponent("No result available."));
        }
        if(fullResult.getType() != ScanType.WORLD){
            throw new CommandException(new StringTextComponent("Result is not of type WORLD."));
        }
        ProfileResult result;
        if(source instanceof ServerPlayerEntity && !hasPerms(sender, Perms.Permission.FULL)){
            long secondsLeft = CPacketRequestScan.secondsLeft(sender.asPlayer().getGameProfile().getId());
            if(secondsLeft > 0){
                throw new CommandException(new StringTextComponent("Please wait " + secondsLeft + " seconds."));
            }
            result = Perms.getResultFor(sender.asPlayer(), fullResult);
        }else{
            result = fullResult;
        }
        msg(sender, "Total ticks", result.getTickCount());
        msg(sender, "Total time", result.getTotalTime()/1000/1000/1000 + " seconds");
        msg(sender, "TPS", Math.round(result.getTPS() * 100D)/100D);
        title(sender, "ENTITIES");
        boolean has = false;
        for(GuiScanResultsWorld.LagSource lagSource : result.getLagSources()){
            if(lagSource.data.type == ObjectData.Type.ENTITY) {
                msg(sender, muPerTickString(lagSource.nanos, result), lagSource.data);
                has = true;
            }
        }
        if(!has){
            sender.sendFeedback(new StringTextComponent("None"), true);
        }
        has = false;
        title(sender, "TILE ENTITIES");
        for(GuiScanResultsWorld.LagSource lagSource : result.getLagSources()){
            if(lagSource.data.type == ObjectData.Type.TILE_ENTITY) {
                msg(sender, muPerTickString(lagSource.nanos, result), lagSource.data);
                has = true;
            }
        }
        if(!has){
            sender.sendFeedback(new StringTextComponent("None"), true);
        }
        has = false;
        title(sender, "BLOCKS");
        for(GuiScanResultsWorld.LagSource lagSource : result.getLagSources()){
            if(lagSource.data.type == ObjectData.Type.BLOCK) {
                msg(sender, muPerTickString(lagSource.nanos, result), lagSource.data);
                has = true;
            }
        }
        if(!has){
            sender.sendFeedback(new StringTextComponent("None"), true);
        }
        has = false;
        title(sender, "EVENTS");
        for(GuiScanResultsWorld.LagSource lagSource : result.getLagSources()){
            if(lagSource.data.type == ObjectData.Type.EVENT_BUS_LISTENER) {
                msg(sender, muPerTickString(lagSource.nanos, result), lagSource.data);
                has = true;
            }
        }
        if(!has){
            sender.sendFeedback(new StringTextComponent("None"), true);
        }
        title(sender, "END");
        sender.sendFeedback(new StringTextComponent("Results printed, copy your log."), true);
    }

    private static void msg(CommandSource sender, String key, Object value){
        sender.sendFeedback(new StringTextComponent(key + ": " + value), true);
    }

    private static void title(CommandSource sender, String title){
        sender.sendFeedback(new StringTextComponent(TextFormatting.GREEN + "---[ " + title + " ]---"), true);
    }

    private static String muPerTickString(long nanos, ProfileResult result) {
        if(result == null){
            return "?";
        }
        return Double.valueOf((nanos / result.getTickCount()) / 1000).intValue() + " micro-s/t";
    }

}
