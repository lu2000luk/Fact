package com.lu2000luk.fact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.Serializable;

public class FactChunk implements Serializable {
    private String owner = "Unknown";
    private int x = 0;
    private int z = 0;

    public FactChunk(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public boolean isAdjacentTo(FactChunk other) {
        return (this.x == other.x + 1 && this.z == other.z) ||
                (this.x == other.x - 1 && this.z == other.z) ||
                (this.x == other.x && this.z == other.z + 1) ||
                (this.x == other.x && this.z == other.z - 1);

    }
}
