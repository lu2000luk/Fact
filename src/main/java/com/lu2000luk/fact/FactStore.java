package com.lu2000luk.fact;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FactStore {
    public static List<FactTeam> cachedTeams = null;
    public static List<FactChunk> cachedChunks = null;

    public static File getTeamsFile() {
        Path gameDir = FMLPaths.CONFIGDIR.get();
        return new File(gameDir.toFile(), "fact_teams.json");
    }

    public static File geChunkFile() {
        Path gameDir = FMLPaths.CONFIGDIR.get();
        return new File(gameDir.toFile(), "fact_chunks.json");
    }

    public static String readFile(File file) throws IOException {
        if (file.createNewFile()) {
            LogUtils.getLogger().info("Created new file from READ: {}", file.getPath());
            writeFile(file, "[]");
        }
        try {
            return new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to read file", e);
        }
        return "";
    }

    public static void writeFile(File file, String content) throws IOException {
        if (file.createNewFile()) {
            LogUtils.getLogger().info("Created new file from WRITE: {}", file.getPath());
            writeFile(file, "[]");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()))) {
            writer.write(content);
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to write file", e);
        }
    }

    public static List<FactTeam> teamSerialize(String content) {
        return Fact.gson.fromJson(content, new TypeToken<List<FactTeam>>() {}.getType());
    }

    public static String teamDeserialize(List<FactTeam> teams) {
        return Fact.gson.toJson(teams);
    }

    public static List<FactChunk> chunkSerialize(String content) {
        return Fact.gson.fromJson(content, new TypeToken<List<FactChunk>>() {}.getType());
    }

    public static String chunkDeserialize(List<FactChunk> chunks) {
        return Fact.gson.toJson(chunks);
    }

    public static List<FactTeam> getTeams() {
        try {
            FactTeam[] teams = teamSerialize(readFile(getTeamsFile())).toArray(new FactTeam[0]);
            return new ArrayList<>(Arrays.asList(teams));
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to get teams", e);
        }
        return List.of();
    }


    public static void setTeams(List<FactTeam> teams) {
        try {
            writeFile(getTeamsFile(), teamDeserialize(teams));
            updateCacheTeams();
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to set teams", e);
        }
    }

    public static List<FactChunk> getChunks() {
        try {
            FactChunk[] chunks = chunkSerialize(readFile(geChunkFile())).toArray(new FactChunk[0]);
            return new ArrayList<>(Arrays.asList(chunks));
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to get chunks", e);
        }
        return List.of();
    }

    public static void setChunks(List<FactChunk> chunks) {
        try {
            writeFile(geChunkFile(), chunkDeserialize(chunks));
            updateCacheChunks();
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to set chunks", e);
        }
    }

    public static void updateCache() {
        updateCacheTeams();
        updateCacheChunks();
    }

    public static void updateCacheTeams() {
        cachedTeams = getTeams();
    }

    public static void updateCacheChunks() {
        cachedChunks = getChunks();
    }
}
