package ca.eelt.featorio.config;

import ca.eelt.featorio.Featorio;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

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

                // Data
                //Holder<Biome> biomeHolder = Holder.direct(Biomes.DESERT);
                AtomicReference<ArrayList<TagKey<Biome>>> whitelistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
                AtomicReference<ArrayList<TagKey<Biome>>> blacklistedBiomeKeys = new AtomicReference<>(new ArrayList<>());
                //Optional<Holder<Feature<?>>> feature = Optional.empty();
                Holder<PlacedFeature> feature = null;
                boolean triangularPlacement = false;
                int count = 0;
                int bottomAnchor = -64;
                int topAnchor = 320;
                int rarity = 10;
                String stepping = "underground_ores";

                // Serialize JSON
                if (masterObject.has("biome")) {
                    //biomeHolder = Holder.direct(ForgeRegistries.BIOMES.getValue(new ResourceLocation(masterObject.get("biome").getAsString())));

                } else if (masterObject.has("whitelisted_biome_keys")
                        || masterObject.has("blacklisted_biome_keys")){

                    if (masterObject.has("whitelisted_biome_keys")){

                        Arrays.stream(masterObject.get("whitelisted_biome_keys").getAsString().split(",")).sequential().forEach(whitelistedKey ->
                                whitelistedBiomeKeys.get().add(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(whitelistedKey))));
                    }

                    if (masterObject.has("blacklisted_biome_keys")){

                        Arrays.stream(masterObject.get("blacklisted_biome_keys").getAsString().split(",")).sequential().forEach(blacklistedKey ->
                                blacklistedBiomeKeys.get().add(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(blacklistedKey))));
                    }
                }

                if (masterObject.has("feature")){
                    //feature = ForgeRegistries.FEATURES.getHolder(new ResourceLocation(masterObject.get("feature").getAsString()));
                    feature = Holder.direct(BuiltinRegistries.PLACED_FEATURE.get(new ResourceLocation(masterObject.get("feature").getAsString())));
                }

                if (masterObject.has("triangular_placement")){
                    triangularPlacement = masterObject.get("triangular_placement").getAsBoolean();
                }

                if (masterObject.has("count")){
                    count = masterObject.get("count").getAsInt();
                }

                if (masterObject.has("bottom_anchor")){
                    bottomAnchor = masterObject.get("bottom_anchor").getAsInt();
                }

                if (masterObject.has("top_anchor")){
                    topAnchor = masterObject.get("top_anchor").getAsInt();
                }

                if (masterObject.has("rarity")){
                    rarity = masterObject.get("rarity").getAsInt();
                }

                if (masterObject.has("stepping")){
                    stepping = masterObject.get("stepping").getAsString();
                }

                System.out.println("Adding addition entry!");
                additionEntries.get().add(
                        new FeatorioFeatureEntry(
                                BiomeModifier.Phase.ADD,
                                //List.of(biomeHolder),
                                whitelistedBiomeKeys.get(),
                                blacklistedBiomeKeys.get(),
                                feature,
                                triangularPlacement,
                                count,
                                bottomAnchor,
                                topAnchor,
                                rarity,
                                stepping
                                )
                );

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

}
