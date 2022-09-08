package ca.eelt.featorio.level;

import ca.eelt.featorio.config.ConfigSerializer;
import ca.eelt.featorio.config.FeatorioFeatureEntry;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FeatorioModifier implements BiomeModifier {

    private boolean active;

    public FeatorioModifier(boolean active){
        this.active = active;
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        System.out.println("Modify called, Phase: " + phase);
        switch (phase){
            case ADD -> runAdditions(biome, builder);
            case MODIFY -> runModifies(biome.get(), builder);
            case REMOVE -> runRemovals(biome.get(), builder);
        }
    }

    private void runAdditions(Holder<Biome> biome, ModifiableBiomeInfo.BiomeInfo.Builder builder){
        System.out.println("Featorio loaded features to be added: " + ConfigSerializer.additionEntries.get().size() );
        for (FeatorioFeatureEntry entry : ConfigSerializer.additionEntries.get()){
            System.out.println("entry: " + entry + " " + entry.manditoryIncludeKeys() + ", " + entry.generationStepping());

//            if (entry.biomes() != null && !entry.biomes().isEmpty()){
//                if (!entry.biomes().get(0).get().equals(biome.get())) return;
            /*} else*/ if (!biomeHasValidTags(
                    entry.manditoryIncludeKeys(),
                    entry.anyExcludeKeys(),
                    biome)){
                return;
            }

            System.out.println("Verified Tags are correct");

            System.out.println("feature().get().getFeatures() size: " + entry.feature().get().getFeatures().toList().size());
            System.out.println("feature().get(): " + entry.feature().get());
            System.out.println("feature().get().feature(): " + entry.feature().get().feature());

            AtomicReference<ModifiableBiomeInfo.BiomeInfo.Builder> atomicBulider = new AtomicReference<>(builder);
            entry.feature().get().getFeatures().toList().forEach(configuredFeature -> {
                int size = 5;
                float discard = 0;
                ConfiguredFeature<?,?> modifiedConfiguredFeature = new ConfiguredFeature<>(Feature.ORE,
                        new OreConfiguration(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Blocks.EMERALD_ORE.defaultBlockState(), size, discard));

                Holder<PlacedFeature> newPlacedFeature = entry.isTriangular() ?
                        Holder.direct(new PlacedFeature(Holder.direct(modifiedConfiguredFeature), List.of(
                                HeightRangePlacement.triangle(VerticalAnchor.absolute(entry.bottomAnchor()), VerticalAnchor.absolute(entry.topAnchor())),
                                CountPlacement.of(entry.count()),
                                RarityFilter.onAverageOnceEvery(entry.rarity()),
                                InSquarePlacement.spread()
                        )))
                        :
                        Holder.direct(new PlacedFeature(Holder.direct(modifiedConfiguredFeature), List.of(
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(entry.bottomAnchor()), VerticalAnchor.absolute(entry.topAnchor())),
                                CountPlacement.of(entry.count()),
                                RarityFilter.onAverageOnceEvery(entry.rarity()),
                                InSquarePlacement.spread()
                        )));

                System.out.println("Featorio is about to add a custom feature!!");
                atomicBulider.get().getGenerationSettings().addFeature(computeStepping(entry.generationStepping()), newPlacedFeature);
            });
        }

    }

    private void runModifies(Biome biome, ModifiableBiomeInfo.BiomeInfo.Builder builder){

    }

    private void runRemovals(Biome biome, ModifiableBiomeInfo.BiomeInfo.Builder builder){

    }

    private boolean biomeHasValidTags(List<TagKey<Biome>> whitelist, List<TagKey<Biome>> blacklist, Holder<Biome> biome){
        boolean wPass = true;
        boolean bPass = true;

        // For whitelists: sizes should be identical when filtering, if so, all the tags needed are present
        if (whitelist != null && !whitelist.isEmpty()){
            System.out.println("WhiteList is not empty or null!");
            // Sizes should be identical
            wPass = biome.getTagKeys().filter(b -> whitelist.contains(b)).count() != whitelist.size();
            System.out.println("Computed wPass: " + wPass);
            if (wPass == false) return false;
        }

        // If any match, return, as a blacklisted tag has been hit
        if (blacklist != null && !blacklist.isEmpty()){
            System.out.println("BlackList is not empty or null!");
            bPass = biome.getTagKeys().noneMatch(blacklist::contains);
            System.out.println("Computed bPass: " + bPass);
            if (bPass == false) return false;
        }

        return wPass == bPass;
    }

    public GenerationStep.Decoration computeStepping(String generationStep){
        // DON'T DO THIS:
//        return GenerationStep.Decoration.valueOf(generationStep);
        // As it'll return: java.lang.IllegalArgumentException: No enum constant net.minecraft.world.level.levelgen.GenerationStep.Decoration.underground_decoration

        if (generationStep.equals("raw_generation")) return GenerationStep.Decoration.RAW_GENERATION;
        if (generationStep.equals("lakes")) return GenerationStep.Decoration.LAKES;
        if (generationStep.equals("local_modifications")) return GenerationStep.Decoration.LOCAL_MODIFICATIONS;
        if (generationStep.equals("underground_structures")) return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
        if (generationStep.equals("surface_structures")) return GenerationStep.Decoration.SURFACE_STRUCTURES;
        if (generationStep.equals("strongholds")) return GenerationStep.Decoration.STRONGHOLDS;
        if (generationStep.equals("underground_ores")) return GenerationStep.Decoration.UNDERGROUND_ORES;
        if (generationStep.equals("underground_decoration")) return GenerationStep.Decoration.UNDERGROUND_DECORATION;
        if (generationStep.equals("fluid_springs")) return GenerationStep.Decoration.FLUID_SPRINGS;
        if (generationStep.equals("vegetal_decoration")) return GenerationStep.Decoration.VEGETAL_DECORATION;
        if (generationStep.equals("top_layer_modification")) return GenerationStep.Decoration.TOP_LAYER_MODIFICATION;
        throw new JsonSyntaxException("Invalid JSON syntax for generation stepping: " + generationStep + ". Generation step can only be: " +
                GenerationStep.Decoration.values());
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return null;
    }


    public boolean getActive(){
        return this.active;
    }

    public void setActive(boolean active){
        this.active = active;
    }
}
