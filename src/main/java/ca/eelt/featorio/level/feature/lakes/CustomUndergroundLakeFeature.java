package ca.eelt.featorio.level.feature.lakes;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class CustomUndergroundLakeFeature extends CustomLakeFeature {

    private int upperBound;
    private int lowerBound;

    public CustomUndergroundLakeFeature(Codec<Configuration> codec, int upperBound, int lowerBound) {
        super(codec);
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        return super.place(
                context.level(),
                context.chunkGenerator(),
                context.random(),
                new BlockPos(
                        context.origin().getX(),
                        (int)(context.random().nextInt(upperBound + lowerBound) - lowerBound),
                        context.origin().getZ()
                ),
                context.config()
        );
    }

}
