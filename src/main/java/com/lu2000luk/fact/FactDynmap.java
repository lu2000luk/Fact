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
                List<FactChunk> perimeterChunks = getConvexHull(chunkGroup);

                double[] x = new double[perimeterChunks.size() * 4];
                double[] z = new double[perimeterChunks.size() * 4];

                Set<String> uniquePositions = new HashSet<>();
                List<Integer> indicesToRemove = new ArrayList<>();

                for (int i = 0; i < perimeterChunks.size(); i++) {
                    FactChunk chunk = perimeterChunks.get(i);
                    String[] positions = new String[4];
                    positions[0] = chunk.getX() * 16 + "," + chunk.getZ() * 16;
                    positions[1] = chunk.getX() * 16 + 16 + "," + chunk.getZ() * 16 + 16;
                    positions[2] = chunk.getX() * 16 + 16 + "," + chunk.getZ() * 16;
                    positions[3] = chunk.getX() * 16 + "," + chunk.getZ() * 16 + 16;

                    for (int j = 0; j < 4; j++) {
                        if (!uniquePositions.add(positions[j])) {
                            indicesToRemove.add(i + j * perimeterChunks.size());
                        }
                    }

                    x[i] = chunk.getX() * 16;
                    z[i] = chunk.getZ() * 16;

                    x[i + perimeterChunks.size()] = chunk.getX() * 16 + 16;
                    z[i + perimeterChunks.size()] = chunk.getZ() * 16;

                    x[i + perimeterChunks.size() * 2] = chunk.getX() * 16 + 16;
                    z[i + perimeterChunks.size() * 2] = chunk.getZ() * 16 + 16;

                    x[i + perimeterChunks.size() * 3] = chunk.getX() * 16;
                    z[i + perimeterChunks.size() * 3] = chunk.getZ() * 16 + 16;
                }

                for (int index : indicesToRemove) {
                    x[index] = Double.NaN;
                    z[index] = Double.NaN;
                }

                x = Arrays.stream(x).filter(Double::isFinite).toArray();
                z = Arrays.stream(z).filter(Double::isFinite).toArray();

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

    private static List<FactChunk> getConvexHull(List<FactChunk> points) {
        if (points.size() <= 1) return points;
        points.sort(Comparator.comparingInt(FactChunk::getX).thenComparingInt(FactChunk::getZ));
        List<FactChunk> hull = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int start = hull.size();
            for (FactChunk point : points) {
                while (hull.size() >= start + 2 && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), point) <= 0) {
                    hull.remove(hull.size() - 1);
                }
                hull.add(point);
            }
            hull.remove(hull.size() - 1);
            Collections.reverse(points);
        }
        if (hull.size() == 2 && hull.get(0).equals(hull.get(1))) hull.remove(1);
        return hull;
    }

    private static int cross(FactChunk o, FactChunk a, FactChunk b) {
        return (a.getX() - o.getX()) * (b.getZ() - o.getZ()) - (a.getZ() - o.getZ()) * (b.getX() - o.getX());
    }

    private static boolean isVertexFacingOutside(double vertexX, double vertexZ, List<FactChunk> perimeterChunks) {
        int n = perimeterChunks.size();
        for (int i = 0; i < n; i++) {
            FactChunk a = perimeterChunks.get(i);
            FactChunk b = perimeterChunks.get((i + 1) % n);
            FactChunk c = perimeterChunks.get((i + 2) % n);
            int crossProduct = (b.getX() - a.getX()) * (c.getZ() - a.getZ()) - (b.getZ() - a.getZ()) * (c.getX() - a.getX());
            if (crossProduct < 0) {
                return true;
            }
        }
        return false;
    }
}
