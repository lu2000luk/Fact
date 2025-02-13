# Fact

A simple open source Minecraft (Forge 1.20.1) mod adds teams and chunk claiming in an admin-based system.

# Features
- Teams
- Chunk claiming
- Admin-based system
- Optimized Dynmap Integration

# Installation
Fact is only available for Minecraft Forge 1.20.1. We have no official support of custom server softwares that allow Forge + Plugin support, don't make issues if you have errors using the mod on those softwares.

1. Download the latest release from the Modrinth page.
2. Place the downloaded file in the `mods` folder of your Minecraft instance.
3. (Optional) Download the latest version of Dynmap from the [Curseforge](https://www.curseforge.com/minecraft/mc-mods/dynmapforge) and place it in the `mods` folder of your Minecraft instance.
4. Start the game and enjoy!

# Usage
- `/fact admin create <name>` - Create a team.
- `/fact admin delete <name>` - Delete a team.
- `/fact admin claim <team name>` - Claim the chunk you are standing in for the specified team.
- `/fact admin unclaim <team name>` - Unclaim the chunk you are standing in for the specified team.
- `/fact admin set_leader <player> ` - Force set the leader of the team you are in.
- `/fact admin leave` - Makes you leave the team you are in.
- `/fact admin join <name>` - Join a team.
- `/fact admin player join <player> <name>` - Makes a player join a team.
- `/fact admin player leave <player>` - Makes a player leave a team.
- `/fact set_leader <player>` - Makes you the leader of the team you are in. (Only works if you are the leader of the team)
- `/fact ally <name>` - Allows another team to mine/place blocks in your land
- `/fact unally <name>` - Disallows another team to mine/place blocks in your land

# Future plans

- Admin UI for viewing claimed chunks
- More mod integrations
- API for accessing the stored data.
- Worldedit-style bulk chunk claiming
- Permissions and Config.
