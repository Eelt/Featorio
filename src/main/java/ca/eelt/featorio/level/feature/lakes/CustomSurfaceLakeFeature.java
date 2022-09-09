package ca.eelt.featorio.level.feature.lakes;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class CustomSurfaceLakeFeature extends CustomLakeFeature{

    public CustomSurfaceLakeFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        return context.level().canSeeSky(context.origin()) && super.place(context);
    }
}
