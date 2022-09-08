package ca.eelt.featorio.level;

import ca.eelt.featorio.Featorio;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Modifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> FEATORIO_MODIFIER_REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Featorio.MODID);

    public static RegistryObject<Codec<FeatorioModifier>> FEATORIO_MODIFIER = FEATORIO_MODIFIER_REGISTRY.register("featorio_modifier", () ->
            RecordCodecBuilder.create(builder -> builder.group(
                    Codec.BOOL.fieldOf("active").forGetter(FeatorioModifier::getActive)
            ).apply(builder, FeatorioModifier::new)));
}
