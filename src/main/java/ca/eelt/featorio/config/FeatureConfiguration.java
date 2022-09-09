package ca.eelt.featorio.config;

public record FeatureConfiguration(boolean isTriangular,
                                   int size,
                                   int count,
                                   int bottomAnchor,
                                   int topAnchor,
                                   int rarity,
                                   float discard) {
}
