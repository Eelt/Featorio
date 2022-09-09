package ca.eelt.featorio.misc;

import com.google.gson.JsonSyntaxException;
import net.minecraft.world.level.levelgen.GenerationStep;

public class Util {

    public Util(){}

    public static GenerationStep.Decoration computeStepping(String generationStep){
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

        StringBuilder steppings = new StringBuilder();
        for (GenerationStep.Decoration stepping : GenerationStep.Decoration.values()){
                steppings.append(stepping.getName()).append(", ");
        }


        throw new JsonSyntaxException("Invalid JSON syntax for generation stepping: " + generationStep + ". Generation step can only be: " + steppings
                );
    }

}
