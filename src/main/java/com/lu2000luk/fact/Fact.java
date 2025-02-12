package com.lu2000luk.fact;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static com.lu2000luk.fact.FactStore.updateCache;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Fact.MODID)
public class Fact
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "fact";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Fact()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Fact >> Common setup");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Fact >> Server starting");
        FactDynmap.register();
        updateCache();
    }

    // Block break event
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        Player player = event.getPlayer();

        handleBlockEvents(event, player);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event)
    {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        handleBlockEvents(event, player);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event)
    {
        LOGGER.info("Fact >> Registering FACT command");
        FactCommand.register(event.getDispatcher());
    }

    public void handleBlockEvents(BlockEvent event, Player player)
    {
        if (player.isCreative()) return;

        Block block = event.getState().getBlock();
        if (block == Blocks.AIR) return;

        LevelChunk chunk = player.getCommandSenderWorld().getChunkAt(event.getPos());
        List<FactChunk> chunkList = FactStore.getChunks();

        FactChunk factChunk = chunkList.stream().filter(c -> c.getX() == chunk.getPos().x && c.getZ() == chunk.getPos().z).findFirst().orElse(null);
        if (factChunk == null) return;

        if (factChunk.getOwner().equals(player.getUUID().toString())) return;
        if (isMemberOrAlly(player, factChunk)) return;

        player.displayClientMessage(Component.literal("You can't do this!"), true);
        event.setCanceled(true);
    }

    private static boolean isMemberOrAlly(Player player, FactChunk chunk)
    {
        String ownerTeamName = chunk.getOwner();
        String UUID = player.getUUID().toString();
        List<FactTeam> teams = FactStore.cachedTeams;
        FactTeam team = teams.stream().filter(t -> t.getName().equals(ownerTeamName)).findFirst().orElse(null);

        if (team == null) return false;
        if (Arrays.stream(team.getMembers()).toList().contains(UUID)) return true;

        String[] allies = team.getAllies();
        for (String ally : allies)
        {
            FactTeam allyTeam = teams.stream().filter(t -> t.getName().equals(ally)).findFirst().orElse(null);
            if (allyTeam == null) continue;
            if (Arrays.stream(allyTeam.getMembers()).toList().contains(UUID)) return true;
        }

        return false;
    }

    public static Gson gson = new Gson();
}
