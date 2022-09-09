package ca.eelt.featorio.config;

import ca.eelt.featorio.Featorio;
import ca.eelt.featorio.misc.Util;
import com.google.gson.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigSerializer {

    public static AtomicReference<ArrayList<FeatorioFeatureEntry>> additionEntries = new AtomicReference<>(new ArrayList<>());
    public static AtomicReference<ArrayList<FeatorioFeatureEntry>> modifyEntries = new AtomicReference<>(new ArrayList<>());
    public static AtomicReference<ArrayList<FeatorioFeatureEntry>> removalEntries = new AtomicReference<>(new ArrayList<>());

    public static void serialize(){
        File additions = new File(Featorio.getAddPath().toUri());
        File removals = new File(Featorio.getRemovePath().toUri());
        File modify = new File(Featorio.getModifyPath().toUri());

        // Additions
        if (additions.exists() && additions.listFiles().length > 0){
            Arrays.asList(additions.listFiles()).parallelStream().filter(file -> file.getPath().endsWith(".json")).forEach(file -> {
                System.out.println("Detected addition file: " + file.getName());

                JsonElement element = getJsonFromFile(file.getPath());
                if (element == null) throw new NullPointerException("Json element is null for file: " + file.getName());
                JsonObject masterObject = element.getAsJsonObject();

                System.out.println("Printing loaded addition JSON " + file.getName() + ": ");
                System.out.println(masterObject.toString());

                // Serialize JSON
                if (masterObject.has("addition_type")){
                    String additionTypeString = masterObject.get("addition_type").getAsString();
                    System.out.println("Addition Type String captured: " + additionTypeString);

                    if (additionTypeString.equals("ore")){
                        additionEntries.get().add(generateOreConfigurationAddition(masterObject));
                    } else if (additionTypeString.equals("block_state_configuration")){
                        //additionType = AdditionType.BLOCK_STATE_CONFIGURATION;
                    } else if (additionTypeString.equals("lake")){
                        additionEntries.get().add(generateLakeConfigurationAddition(masterObject));
                    }
                }

            });
        }

        // Removals
        if (removals.exists() && removals.listFiles().length > 0){
            Arrays.asList(removals.listFiles()).parallelStream().filter(file -> file.getPath().endsWith(".json")).forEach(file -> {
                System.out.println("Detected removal file: " + file.getName());

                JsonElement element = getJsonFromFile(file.getPath());
                if (element == null) throw new NullPointerException("Json element is null for file: " + file.getName());
                JsonObject masterObject = element.getAsJsonObject();

                System.out.println("Printing loaded removal JSON " + file.getName() + ": ");
                System.out.println(masterObject.toString());
            });
        }

        // Modifys
        if (modify.exists() && modify.listFiles().length > 0){
            Arrays.asList(modify.listFiles()).parallelStream().filter(file -> file.getPath().endsWith(".json")).forEach(file -> {
                System.out.println("Detected modify file: " + file.getName());

                JsonElement element = getJsonFromFile(file.getPath());
                if (element == null) throw new NullPointerException("Json element is null for file: " + file.getName());
                JsonObject masterObject = element.getAsJsonObject();

                System.out.println("Printing loaded modify JSON " + file.getName() + ": ");
                System.out.println(masterObject.toString());
            });
        }
    }

    private static FeatorioFeatureEntry generateOreConfigurationAddition(JsonObject masterObject){
        AdditionType additionType = AdditionType.ORE_CONFIGURATION;
        //Holder<Biome> biomeHolder = Holder.direct(Biomes.DESERT);
        AtomicReference<ArrayList<TagKey<Biome>>> whitelistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
        AtomicReference<ArrayList<TagKey<Biome>>> blacklistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
        List<OreConfiguration.TargetBlockState> targetBlockStates = null;
        FeatureConfiguration featureConfiguration = null;
        GenerationStep.Decoration stepping;

        // serialize and process biome data
        if (masterObject.has("biome_spawns")){
            Pair<List<TagKey<Biome>>,List<TagKey<Biome>>> serializedBiomeKeys = buildBiomeTagKeys(masterObject.getAsJsonObject("biome_spawns"));

            whitelistedBiomeKeys.get().addAll(serializedBiomeKeys.getA());
            blacklistedBiomeKeys.get().addAll(serializedBiomeKeys.getB());
        } else throw new JsonParseException("Can't find biome_spawns in JSON!");

        // serialize and process blockstate targeting data
        if (masterObject.has("blockstates")){
            targetBlockStates = buildTargettedBlockstateData(masterObject.getAsJsonArray("blockstates"));
        } else throw new JsonParseException("Can't find blockstates in JSON!");

        // Serialize and process the main configuration of the feature to be added
        if (masterObject.has("configuration")){
            featureConfiguration = buildFeatureConfiguration(masterObject.get("configuration").getAsJsonObject());
        } else throw new JsonParseException("Can't find configuration in JSON!");

        if (masterObject.has("generation_step")){
            stepping = Util.computeStepping(masterObject.get("generation_step").getAsString());
        } else throw new JsonParseException("Can't find generation_step in JSON!");

        return new FeatorioFeatureEntry(
                BiomeModifier.Phase.ADD,
                additionType,
                whitelistedBiomeKeys.get(),
                blacklistedBiomeKeys.get(),
                targetBlockStates,
                null,
                false,
                featureConfiguration,
                stepping
        );
    }

    private static FeatorioFeatureEntry generateLakeConfigurationAddition(JsonObject masterObject){
        AdditionType additionType = AdditionType.LAKE_CONFIGURATION;
        //Holder<Biome> biomeHolder = Holder.direct(Biomes.DESERT);
        AtomicReference<ArrayList<TagKey<Biome>>> whitelistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
        AtomicReference<ArrayList<TagKey<Biome>>> blacklistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
        Pair<BlockState,BlockState> lakeStates = null;
        boolean generatePerimeter;
        FeatureConfiguration featureConfiguration = null;
        GenerationStep.Decoration stepping;

        // serialize and process biome data
        if (masterObject.has("biome_spawns")){
            Pair<List<TagKey<Biome>>,List<TagKey<Biome>>> serializedBiomeKeys = buildBiomeTagKeys(masterObject.getAsJsonObject("biome_spawns"));

            whitelistedBiomeKeys.get().addAll(serializedBiomeKeys.getA());
            blacklistedBiomeKeys.get().addAll(serializedBiomeKeys.getB());
        } else throw new JsonParseException("Can't find biome_spawns in JSON!");

        // serialize and process blockstate data for fluid and for perimeter
        if (masterObject.has("blockstates")){
            lakeStates = buildLakeStates(masterObject.getAsJsonObject("blockstates"));
        } else throw new JsonParseException("Can't find blockstates in JSON!");

        // Determine if perimeter should be generated. If omitted do null check on getB() for lake states as that's the BS that would be used for a perimeter
        if (masterObject.has("generate_perimeter")){
            generatePerimeter = masterObject.get("generate_perimeter").getAsBoolean();
        } else generatePerimeter = lakeStates.getB() != null;

        // Serialize and process the main configuration of the feature to be added
        if (masterObject.has("configuration")){
            featureConfiguration = buildFeatureConfiguration(masterObject.get("configuration").getAsJsonObject());
        } else throw new JsonParseException("Can't find configuration in JSON!");

        if (masterObject.has("generation_step")){
            stepping = Util.computeStepping(masterObject.get("generation_step").getAsString());
        } else throw new JsonParseException("Can't find generation_step in JSON!");

        return new FeatorioFeatureEntry(
                BiomeModifier.Phase.ADD,
                additionType,
                whitelistedBiomeKeys.get(),
                blacklistedBiomeKeys.get(),
                null,
                lakeStates,
                generatePerimeter,
                featureConfiguration,
                stepping
        );
    }

    private static Pair<List<TagKey<Biome>>,List<TagKey<Biome>>> buildBiomeTagKeys(JsonObject biomeObject){
        AtomicReference<ArrayList<TagKey<Biome>>> whitelistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
        AtomicReference<ArrayList<TagKey<Biome>>> blacklistedBiomeKeys = new AtomicReference<>(new ArrayList<>());

        if (biomeObject.has("whitelisted_biome_keys")){
            String whitelist = biomeObject.get("whitelisted_biome_keys").getAsString();

            if (!whitelist.equals("")){
                Arrays.stream(whitelist.split(",")).sequential().forEach(whitelistedKey ->
                        whitelistedBiomeKeys.get().add(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(whitelistedKey))));
            }

        }

        if (biomeObject.has("blacklisted_biome_keys")) {
            String blacklist = biomeObject.get("blacklisted_biome_keys").getAsString();

            if (!blacklist.equals("")) {
                Arrays.stream(blacklist.split(",")).sequential().forEach(blacklistedKey ->
                        blacklistedBiomeKeys.get().add(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(blacklistedKey))));
            }

        }

        return new Pair<>(whitelistedBiomeKeys.get(), blacklistedBiomeKeys.get());
    }

    private static List<OreConfiguration.TargetBlockState> buildTargettedBlockstateData(JsonArray blockstateArray){
        AtomicReference<ArrayList<OreConfiguration.TargetBlockState>> oreTargets = new AtomicReference<>(new ArrayList<>());

        blockstateArray.forEach(jsonElement -> {
            JsonArray jsonStatePair = jsonElement.getAsJsonArray();
            assert jsonStatePair.size() == 2; // Assert pairwise. Tag to target + the block that'll be placed on the validly targeted blocks

            // Build the target
            OreConfiguration.TargetBlockState oreTarget = OreConfiguration.target(
                    new TagMatchTest(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(jsonStatePair.get(0).getAsString()))),
                    ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonStatePair.get(1).getAsString())).defaultBlockState()
            );

            oreTargets.get().add(oreTarget);
        });

        return oreTargets.get();
    }

    private static FeatureConfiguration buildFeatureConfiguration(JsonObject configurationObject){
        boolean triangularPlacement = false;
        int size = 0;
        int count = 0;
        int bottomAnchor = -64;
        int topAnchor = 320;
        int rarity = 0;
        float discard = 0;

        if (configurationObject.has("triangular_placement")){
            triangularPlacement = configurationObject.get("triangular_placement").getAsBoolean();
        }

        if (configurationObject.has("size")){
            size = configurationObject.get("size").getAsInt();
        }

        if (configurationObject.has("count")){
            count = configurationObject.get("count").getAsInt();
        }

        if (configurationObject.has("bottom_anchor")){
            bottomAnchor = configurationObject.get("bottom_anchor").getAsInt();
            assert bottomAnchor > -65 && bottomAnchor < 321;
        }

        if (configurationObject.has("top_anchor")){
            topAnchor = configurationObject.get("top_anchor").getAsInt();
            assert topAnchor > -65 && topAnchor < 321;
        }

        if (configurationObject.has("rarity")){
            rarity = configurationObject.get("rarity").getAsInt();
        }

        if (configurationObject.has("discard")){
            discard = configurationObject.get("discard").getAsFloat();
        }

        return new FeatureConfiguration(triangularPlacement, size, count, bottomAnchor, topAnchor, rarity, discard);
    }

    private static Pair<BlockState,BlockState> buildLakeStates(JsonObject blockStates){ // A = Fluid; B = Perimeter
        ResourceLocation fluidResource = null;
        ResourceLocation perimeterResource = null;

        if (blockStates.has("fluid")){
            fluidResource = new ResourceLocation(blockStates.get("fluid").getAsString());
        } else throw new JsonParseException("Can't find 'fluid' inside blockstates in JSON!");

        if (blockStates.has("perimeter")){ // Optional
            perimeterResource = new ResourceLocation(blockStates.get("perimeter").getAsString());
        }

        BlockState fluidBlockState = ForgeRegistries.BLOCKS.getValue(fluidResource).defaultBlockState();
        BlockState perimeterBlockState = null;
        if (perimeterResource != null) perimeterBlockState = ForgeRegistries.BLOCKS.getValue(perimeterResource).defaultBlockState();

        return perimeterBlockState == null ? new Pair<>(fluidBlockState, null) : new Pair<>(fluidBlockState, perimeterBlockState);
    }

    public static JsonElement getJsonFromFile(String path) {
        JsonElement jsonElement;
        try {
            Reader reader = Files.newBufferedReader(Path.of(path));
            Gson gson = new Gson();
            jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
            reader.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        return jsonElement;
    }

    public enum AdditionType {
        ORE_CONFIGURATION,
        LAKE_CONFIGURATION,
    }
}
