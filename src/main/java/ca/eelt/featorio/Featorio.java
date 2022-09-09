package ca.eelt.featorio;

import ca.eelt.featorio.config.ConfigSerializer;
import ca.eelt.featorio.level.Modifiers;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(Featorio.MODID)
public class Featorio {
    public static final String MODID = "featorio";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static Path basePath;
    private static Path addPath;
    private static Path removePath;
    private static Path modifyPath;

    public Featorio() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Deferred registration occurs here
        Modifiers.FEATORIO_MODIFIER_REGISTRY.register(modEventBus);

        // Build Featorio directory
        basePath = FMLPaths.CONFIGDIR.get().resolve(Featorio.MODID);
        LOGGER.debug("BasePath: " + basePath);
        FileUtils.getOrCreateDirectory(basePath, Featorio.MODID);

        addPath = basePath.resolve("additions");
        removePath = basePath.resolve("removals");
        modifyPath = basePath.resolve("modify");

        FileUtils.getOrCreateDirectory(addPath, "additions");
        FileUtils.getOrCreateDirectory(removePath, "removals");
        FileUtils.getOrCreateDirectory(modifyPath, "modify");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        ConfigSerializer.serialize();
        System.out.println("Addition Entries size after serialization: " + ConfigSerializer.additionEntries.get().size());
        ConfigSerializer.additionEntries.get().forEach(entry -> {
            System.out.println("Include Keys: " + entry.mandatoryIncludeKeys());
            System.out.println("Exclude Keys: " + entry.anyExcludeKeys());
            System.out.println("Stepping: " + entry.generationStepping());
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public static Path getBasePath() {
        return basePath;
    }

    public static Path getAddPath() {
        return addPath;
    }

    public static Path getRemovePath() {
        return removePath;
    }

    public static Path getModifyPath() {
        return modifyPath;
    }
}
