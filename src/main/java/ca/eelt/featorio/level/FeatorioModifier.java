package ca.eelt.featorio.level;

import ca.eelt.featorio.config.ConfigSerializer;
import ca.eelt.featorio.config.FeatorioFeatureEntry;
import ca.eelt.featorio.level.feature.Features;
import ca.eelt.featorio.level.feature.lakes.CustomLakeFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;

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
            System.out.println("entry: " + entry + " " + entry.mandatoryIncludeKeys() + ", " + entry.generationStepping());

//            if (entry.biomes() != null && !entry.biomes().isEmpty()){
//                if (!entry.biomes().get(0).get().equals(biome.get())) return;
            /*} else*/ if (!biomeHasValidTags(
                    entry.mandatoryIncludeKeys(),
                    entry.anyExcludeKeys(),
                    biome)){
                return;
            }

            System.out.println("Verified Tags are correct");

            ConfiguredFeature<?,?> modifiedConfiguredFeature = null;

//            if (entry.additionType() == ConfigSerializer.AdditionType.ORE_CONFIGURATION){
//                modifiedConfiguredFeature = new ConfiguredFeature<>(Feature.ORE,
//                        new OreConfiguration(entry.targetBlockStates(), entry.getSize(), entry.getDiscard()));
//            } else if (entry.additionType() == ConfigSerializer.AdditionType.BLOCK_STATE_CONFIGURATION){
//            } else if (entry.additionType() == ConfigSerializer.AdditionType.LAKE_CONFIGURATION){
//            } else if (entry.additionType() == ConfigSerializer.AdditionType.SURFACE_LAKE_CONFIGURATION){
//            } else if (entry.additionType() == ConfigSerializer.AdditionType.UNDERGROUND_LAKE_CONFIGURATION){
//            } else throw new RuntimeException("The Featorio addition entry type: " + entry.additionType() + " doesn't have logic in FeatorioModifier to generate the placed feature and add it to a biome. This is a bug! Please open an issue on our GitHub with this error message (and logs please!)");

            modifiedConfiguredFeature = switch (entry.additionType()){
                case ORE_CONFIGURATION -> new ConfiguredFeature<>(Feature.ORE,
                        new OreConfiguration(entry.targetBlockStates(), entry.getSize(), entry.getDiscard()));
//                case BLOCK_STATE_CONFIGURATION -> null;
                case LAKE_CONFIGURATION -> new ConfiguredFeature<>(Features.CUSTOM_LAKE_FEATURE.get(),
                        new CustomLakeFeature.Configuration(
                                BlockStateProvider.simple(entry.lakeStates().getA()),
                                BlockStateProvider.simple(entry.lakeStates().getB()),
                                entry.generatePerimeter()
                        ));
                default -> throw new RuntimeException("The Featorio addition entry type: " + entry.additionType() + " doesn't have logic in FeatorioModifier to generate the placed feature and add it to a biome. This is a bug! Please open an issue on our GitHub with this error message (and logs please!)");
            };

            Holder<PlacedFeature> newPlacedFeature = entry.getIsTriangular() ?
                    Holder.direct(new PlacedFeature(Holder.direct(modifiedConfiguredFeature), List.of(
                            HeightRangePlacement.triangle(VerticalAnchor.absolute(entry.getBottomAnchor()), VerticalAnchor.absolute(entry.getTopAnchor())),
                            CountPlacement.of(entry.getCount()),
                            RarityFilter.onAverageOnceEvery(entry.getRarity()),
                            InSquarePlacement.spread()
                    )))
                    :
                    Holder.direct(new PlacedFeature(Holder.direct(modifiedConfiguredFeature), List.of(
                            HeightRangePlacement.uniform(VerticalAnchor.absolute(entry.getBottomAnchor()), VerticalAnchor.absolute(entry.getTopAnchor())),
                            CountPlacement.of(entry.getCount()),
                            RarityFilter.onAverageOnceEvery(entry.getRarity()),
                            InSquarePlacement.spread()
                    )));

            System.out.println("Featorio is about to add a custom feature!!");
            builder.getGenerationSettings().addFeature(entry.generationStepping(), newPlacedFeature);

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
        if (whitelist != null && !whitelist.isEmpty() && whitelist.size() > 0){
            System.out.println("WhiteList is not empty or null!");
            // Sizes should be identical
            wPass = biome.getTagKeys().filter(b -> whitelist.contains(b)).count() == whitelist.size();
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
