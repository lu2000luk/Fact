package com.lu2000luk.fact;

import java.io.Serializable;

public class FactTeam implements Serializable {
    private String name = "Unknown";
    private String leader = "Unknown";
    private String[] allies = new String[0];
    private String[] members = new String[0];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String[] getAllies() {
        return allies;
    }

    public void setAllies(String[] allies) {
        this.allies = allies;
    }

    public String[] getMembers() {
        return members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }
}
