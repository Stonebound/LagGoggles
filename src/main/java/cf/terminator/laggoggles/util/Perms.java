package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.config.Config;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.server.RequestDataHandler;
import cf.terminator.laggoggles.config.ServerConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.UUID;

public class Perms {

    public static final double MAX_RANGE_FOR_PLAYERS_HORIZONTAL_SQ =
            Config.SERVER.NON_OPS_MAX_HORIZONTAL_RANGE.get() * Config.SERVER.NON_OPS_MAX_HORIZONTAL_RANGE.get();
    public static final double MAX_RANGE_FOR_PLAYERS_VERTICAL_SQ =
            Config.SERVER.NON_OPS_MAX_VERTICAL_RANGE.get() * Config.SERVER.NON_OPS_MAX_HORIZONTAL_RANGE.get();

    public enum Permission {
        NONE,
        GET,
        START,
        FULL
    }

    public static Permission getPermission(PlayerEntity p) {

        if (p.hasPermissionLevel(1) || !ServerLifecycleHooks.getCurrentServer().isDedicatedServer()) {
            return Permission.FULL;
        } else {
            return Config.SERVER.NON_OP_PERMISSION_LEVEL.get();
        }
    }

    public static boolean hasPermission(PlayerEntity player, Permission permission) {
        return getPermission(player).ordinal() >= permission.ordinal();
    }

    public static ArrayList<ServerPlayerEntity> getLagGogglesUsers() {
        ArrayList<ServerPlayerEntity> list = new ArrayList<>();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return list;
        }
        for (UUID uuid : RequestDataHandler.playersWithLagGoggles) {
            Entity entity = server.getEntityByUuid(uuid);
            if (entity instanceof ServerPlayerEntity) {
                list.add((ServerPlayerEntity) entity);
            }
        }
        return list;
    }

    public static ProfileResult getResultFor(ServerPlayerEntity player, ProfileResult result) {
        Permission permission = getPermission(player);
        if (permission == Permission.NONE) {
            return ProfileResult.EMPTY_RESULT;
        }
        if (permission == Permission.FULL) {
            return result;
        }
        ProfileResult trimmedResult = result.copyStatsOnly();
        for (ObjectData data : result.getData()) {
            if (isInRange(data, player) == true) {
                trimmedResult.addData(data);
            }
        }
        return trimmedResult;
    }

    public static boolean isInRange(ObjectData data, ServerPlayerEntity player) {
        if (data.type == ObjectData.Type.EVENT_BUS_LISTENER) {
            return Config.SERVER.ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS.get();
        }
        if (data.<Integer>getValue(ObjectData.Entry.WORLD_ID) != player.dimension.getId()) {
            return false;
        }
        switch (data.type) {
            case ENTITY:
                ServerWorld world = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(),
                        DimensionType.getById(data.getValue(ObjectData.Entry.WORLD_ID)), false, false);
                Entity e;
                if (world != null && (e = world.getEntityByUuid(data.getValue(ObjectData.Entry.ENTITY_UUID))) != null) {
                    return checkRange(player, e.posX, e.posY, e.posZ);
                }
                return false;
            case BLOCK:
            case TILE_ENTITY:
                return checkRange(player, data.getValue(ObjectData.Entry.BLOCK_POS_X), data.getValue(ObjectData.Entry.BLOCK_POS_Y),
                        data.getValue(ObjectData.Entry.BLOCK_POS_Z));
            default:
                return false;
        }
    }

    public static boolean checkRange(ServerPlayerEntity player, Integer x, Integer y, Integer z) {
        return checkRange(player, x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    public static boolean checkRange(ServerPlayerEntity player, double x, double y, double z) {
        double xD = x - player.posX;
        double zD = z - player.posZ;

        /* Check horizontal range */
        if (xD * xD + zD * zD > MAX_RANGE_FOR_PLAYERS_HORIZONTAL_SQ) {
            return false;
        }

        /* If it's within range, we check if the Y level is whitelisted */
        if (y > Config.SERVER.NON_OPS_WHITELIST_HEIGHT_ABOVE.get()) {
            return true;
        }

        /* If it's underground, we restrict the results so you can't abuse it to find spawners, chests, minecarts.. etc. */
        double yD = y - player.posY;
        if (yD * yD > MAX_RANGE_FOR_PLAYERS_VERTICAL_SQ) {
            return false;
        }
        return true;
    }
}
