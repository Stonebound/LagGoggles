package cf.terminator.laggoggles.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class Teleport {

    public static void teleportPlayer(ServerPlayerEntity player, DimensionType dim, double x, double y, double z) {
        new RunInServerThread(new Runnable() {
            @Override
            public void run() {
                if (player.dimension != dim) {
                    teleportPlayerToDimension(player, dim, x, y, z);
                } else {
                    player.setPositionAndUpdate(x, y, z);
                }
                player.sendMessage(new StringTextComponent(
                        TextFormatting.GREEN + "Teleported to: " + TextFormatting.GRAY + " Dim: " + dim + TextFormatting.WHITE + ", " + (int) x + ", "
                                + (int) y + ", " + (int) z));
            }
        });
    }

    private static void teleportPlayerToDimension(ServerPlayerEntity playerIn, DimensionType suggestedDimension, double x, double y, double z) {
        ServerWorld world = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), suggestedDimension, false, false); //
        playerIn.teleport(world, x, y, z, playerIn.getYaw(1.0F), playerIn.getPitch(1.0F));
    }
}
