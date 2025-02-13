package com.lu2000luk.fact;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.sort;

public class FactDynmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static DynmapCommonAPI dynmapAPI;

    public static void register() {
        if (!ModList.get().isLoaded("dynmap")) {
            LOGGER.info("Fact >> This mod has compatibility with Dynmap, but Dynmap is not installed.");
            return;
        }

        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI api) {
                LOGGER.info("Fact >> Dynmap API enabled.");

                dynmapAPI = api;

                reloadMarkers(api);
            }
        });
    }

    public static void reloadMarkers(DynmapCommonAPI api) {
        if (api == null) {
            return;
        }

        List<FactTeam> teams = FactStore.getTeams();
        List<FactChunk> chunks = FactStore.getChunks();

        MarkerAPI markerAPI = api.getMarkerAPI();

        String markerSetID = "fact_teams";
        String markerSetLabel = "Fact Teams";

        if (markerAPI.getMarkerSet(markerSetID) == null) {
            LOGGER.info("Fact >> Creating marker set: " + markerSetID);
            markerAPI.createMarkerSet(markerSetID, markerSetLabel, null, false);
        }

        MarkerSet set = markerAPI.getMarkerSet(markerSetID);
        if (set != null) {
            for (AreaMarker marker : set.getAreaMarkers()) {
                marker.deleteMarker();
            }
        }

        for (FactTeam team : teams) {
            List<FactChunk> teamChunks = new ArrayList<>();
            for (FactChunk chunk : chunks) {
                if (Objects.equals(chunk.getOwner(), team.getName())) {
                    teamChunks.add(chunk);
                }
            }

            List<List<FactChunk>> chunkGroups = new ArrayList<>();

            for (FactChunk chunk : teamChunks) {
                boolean added = false;
                for (List<FactChunk> chunkGroup : chunkGroups) {
                    for (FactChunk groupChunk : chunkGroup) {
                        if (chunk.isAdjacentTo(groupChunk)) {
                            chunkGroup.add(chunk);
                            added = true;
                            break;
                        }
                    }
                }

                if (!added) {
                    List<FactChunk> newGroup = new ArrayList<>();
                    newGroup.add(chunk);
                    chunkGroups.add(newGroup);
                }
            }

            for (List<FactChunk> chunkGroup : chunkGroups) {
                double[] pre_x = new double[chunkGroup.size() * 4];
                double[] pre_z = new double[chunkGroup.size() * 4];

                Arrays.fill(pre_x, Double.NaN);
                Arrays.fill(pre_z, Double.NaN);

                LOGGER.info("Fact >> Chunk Group Arrays pre sizes -> X: " + pre_x.length + " | Z: " + pre_z.length);

                for (int i = 0; i < chunkGroup.size(); i++) {
                    FactChunk chunk = chunkGroup.get(i);
                    int x = chunk.getX() * 16;
                    int z = chunk.getZ() * 16;

                    LOGGER.info("Fact >> Chunk pre -> X: " + x + " | Z: " + z + " | From: " + chunk.getX() + " " + chunk.getZ());

                    pre_x[i * 4] = x;
                    pre_z[i * 4] = z;

                    pre_x[i * 4 + 1] = x + 16;
                    pre_z[i * 4 + 1] = z;

                    pre_x[i * 4 + 2] = x + 16;
                    pre_z[i * 4 + 2] = z + 16;

                    pre_x[i * 4 + 3] = x;
                    pre_z[i * 4 + 3] = z + 16;

                    LOGGER.info("Fact >> Chunk post -> X: " + Arrays.toString(pre_x) + " | Z: " + Arrays.toString(pre_z));
                }

                LOGGER.info("Fact >> Chunk Group Arrays post sizes -> X: " + pre_x.length + " | Z: " + pre_z.length);

                LOGGER.info("Fact >> Hulling chunk group of " + chunkGroup.size() + " chunks...");
                OrthogonalConvexHull.Result hull = OrthogonalConvexHull.findOrthogonalConvexHull(pre_x, pre_z);

                double[] x = hull.xCoordinates;
                double[] z = hull.yCoordinates;

                LOGGER.info("Fact >> Before hull: " + Arrays.toString(pre_x) + " " + Arrays.toString(pre_z) + " | After hull: " + Arrays.toString(x) + " " + Arrays.toString(z));

                String teamName = team.getName();
                int teamColor = getColorFromName(teamName);

                String markerID = "fact_team_" + teamName + "_" + chunkGroups.indexOf(chunkGroup);

                if (set == null) {
                    LOGGER.error("Fact >> Marker set not found: " + markerSetID);
                    return;
                }

                String world = ServerLifecycleHooks.getCurrentServer().getWorldData().getLevelName();

                AreaMarker marker = set.createAreaMarker(markerID, teamName, false, world, x, z, false);
                if (marker == null) {
                    LOGGER.error("Fact >> Marker not created: " + markerID + " for team " + teamName + " in " + world);
                    return;
                }

                marker.setDescription(teamName);
                marker.setLineStyle(1, 1.0, teamColor);
                marker.setFillStyle(0.3, teamColor);

                LOGGER.info("Fact >> Created marker: " + markerID);
            }
        }
    }

    public static int getColorFromName(String name) {
        // Hash the name to get a color
        int hash = name.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        return (r << 16) | (g << 8) | b;
    }
}
