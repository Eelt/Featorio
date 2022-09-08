package ca.eelt.featorio.config;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record FeatorioFeatureEntry(BiomeModifier.Phase phase,
                                   //@Nullable List<Holder<Biome>> biomes,
                                   @Nullable List<TagKey<Biome>> manditoryIncludeKeys,
                                   @Nullable List<TagKey<Biome>> anyExcludeKeys,
                                   Holder<PlacedFeature> feature,
                                   //Optional<Holder<net.minecraft.world.level.levelgen.feature.Feature<?>>> feature,
                                   boolean isTriangular,
                                   int count,
                                   int bottomAnchor,
                                   int topAnchor,
                                   int rarity,
                                   String generationStepping) {

}
