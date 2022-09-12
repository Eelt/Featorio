package ca.eelt.featorio.config.entries;

import ca.eelt.featorio.config.ConfigSerializer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import javax.annotation.Nullable;

public record FeatorioRemovalEntry (ConfigSerializer.FeatureType featureType,
                                    @Nullable net.minecraft.world.level.levelgen.feature.Feature<?> feature,
                                    @Nullable PlacedFeature placement) {

}
