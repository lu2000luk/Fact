package com.lu2000luk.fact;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FactStore {
    public static List<FactTeam> cachedTeams = null;

    public static File getFile() {
        Path gameDir = FMLPaths.CONFIGDIR.get();
        return new File(gameDir.toFile(), "fact_teams.json");
    }

    public static String readFile(File file) throws IOException {
        file.createNewFile();
        try {
            return new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to read file", e);
        }
        return "";
    }

    public static void writeFile(File file, String content) throws IOException {
        file.createNewFile();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()))) {
            writer.write(content);
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to write file", e);
        }
    }

    public static FactTeam[] serialize(String content) {
        return Fact.gson.fromJson(content, FactTeam[].class);
    }

    public static String deserialize(FactTeam[] teams) {
        return Fact.gson.toJson(teams);
    }

    public static List<FactTeam> getTeams() {
        try {
            return Arrays.asList(serialize(readFile(getFile())));
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to get teams", e);
        }
        return Arrays.asList(new FactTeam[0]);
    }

    public static void setTeams(FactTeam[] teams) {
        try {
            writeFile(getFile(), deserialize(teams));
            updateCache();
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to set teams", e);
        }
    }

    public static void updateCache() {
        cachedTeams = getTeams();
    }
}
