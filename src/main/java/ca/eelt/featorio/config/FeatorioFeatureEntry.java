package ca.eelt.featorio.config;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraftforge.common.world.BiomeModifier;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.List;

public record FeatorioFeatureEntry(BiomeModifier.Phase phase,
                                   ConfigSerializer.AdditionType additionType,
                                   //@Nullable List<Holder<Biome>> biomes,
                                   @Nullable List<TagKey<Biome>> mandatoryIncludeKeys,
                                   @Nullable List<TagKey<Biome>> anyExcludeKeys,
                                   @Nullable List<OreConfiguration.TargetBlockState> targetBlockStates,// Ore generation
                                   @Nullable Pair<BlockState,BlockState> lakeStates, // Lake Blockstates, A = Fluid, B = Perimeter
                                   boolean generatePerimeter, // Lake: Generate a perimeter around the lake
                                   FeatureConfiguration featureConfiguration,
                                   GenerationStep.Decoration generationStepping) {

    // Pull values from Feature Configuration
    public boolean getIsTriangular(){
        return this.featureConfiguration.isTriangular();
    }

    public int getSize(){
        return this.featureConfiguration.size();
    }

    public int getCount(){
        return this.featureConfiguration.count();
    }

    public Pair<Integer,Integer> getAnchors(){ // A = Bottom; B = Top
        return new Pair<>(this.featureConfiguration.bottomAnchor(), this.featureConfiguration.topAnchor());
    }

    public int getBottomAnchor(){
        return this.featureConfiguration.bottomAnchor();
    }

    public int getTopAnchor(){
        return this.featureConfiguration.topAnchor();
    }

    public int getRarity(){
        return this.featureConfiguration.rarity();
    }

    public float getDiscard(){
        return this.featureConfiguration.discard();
    }
}
