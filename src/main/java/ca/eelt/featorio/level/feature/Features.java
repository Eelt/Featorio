package ca.eelt.featorio.level.feature;

import ca.eelt.featorio.Featorio;
import ca.eelt.featorio.level.feature.lakes.CustomLakeFeature;
import ca.eelt.featorio.level.feature.lakes.CustomSurfaceLakeFeature;
import ca.eelt.featorio.level.feature.lakes.CustomUndergroundLakeFeature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Features {
    public static DeferredRegister<net.minecraft.world.level.levelgen.feature.Feature<?>> FEATORIO_FEATURE_REGISTRY =
            DeferredRegister.create(ForgeRegistries.FEATURES, Featorio.MODID);

    public static RegistryObject<CustomLakeFeature> CUSTOM_LAKE_FEATURE = FEATORIO_FEATURE_REGISTRY.register("custom_lake_feature", () ->
            new CustomLakeFeature(CustomLakeFeature.Configuration.CODEC));

    public static RegistryObject<CustomSurfaceLakeFeature> CUSTOM_SURFACE_LAKE_FEATURE = FEATORIO_FEATURE_REGISTRY.register("custom_surface_lake_feature", () ->
            new CustomSurfaceLakeFeature(CustomLakeFeature.Configuration.CODEC));

    public static RegistryObject<CustomUndergroundLakeFeature> CUSTOM_UNDERGROUND_LAKE_FEATURE = FEATORIO_FEATURE_REGISTRY.register("custom_underground_lake_feature", () ->
            new CustomUndergroundLakeFeature(CustomLakeFeature.Configuration.CODEC, 62, -64));
}
