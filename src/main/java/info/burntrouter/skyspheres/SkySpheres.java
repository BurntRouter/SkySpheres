package info.burntrouter.skyspheres;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

@Mod(SkySpheres.MODID)
public class SkySpheres {
    public static final String MODID = "skyspheres";
    public static final String NAME = "Sky Spheres";
    public static final String VERSION = "1.0.6";

    private static final Map<Biome, List<BlockState>> BIOME_ORE_MAP = new HashMap<>();
    private static final Logger logger = Logger.getLogger(SkySpheres.class.getName());
    private static final List<String> blockDenyList = Arrays.asList(SphereConfig.blockDenyList.get().split(","));

    public SkySpheres() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onConfigLoad);
    }

    private void setup(final FMLCommonSetupEvent event) {
        initializeOreMap();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote()) {
            MinecraftForge.EVENT_BUS.register(new SphereWorldGenerator());
        }
    }

    @SubscribeEvent
    public void onConfigLoad(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            SphereConfig.loadConfig(SphereConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("skyspheres-common.toml"));
        }
    }

    public static class SphereConfig {
        public static final ForgeConfigSpec COMMON_CONFIG;
        static final SphereConfig COMMON;

        public final ForgeConfigSpec.IntValue minHeight;
        public final ForgeConfigSpec.IntValue maxHeight;
        public final ForgeConfigSpec.DoubleValue density;
        public final ForgeConfigSpec.IntValue maxSphereSize;
        public final ForgeConfigSpec.IntValue minSphereSize;
        public final ForgeConfigSpec.DoubleValue oreChance;
        public static ForgeConfigSpec.ConfigValue<String> blockDenyList = null;

        static {
            final Pair<SphereConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SphereConfig::new);
            COMMON_CONFIG = specPair.getRight();
            COMMON = specPair.getLeft();
        }

        SphereConfig(ForgeConfigSpec.Builder builder) {
            minHeight = builder
                    .comment("Minimum height at which spheres can generate")
                    .defineInRange("minHeight", 180, 0, 256);

            maxHeight = builder
                    .comment("Maximum height at which spheres can generate")
                    .defineInRange("maxHeight", 230, 0, 256);

            density = builder
                    .comment("Density of sphere generation (0.0 to 1.0)")
                    .defineInRange("density", 0.03, 0.0, 1.0);

            maxSphereSize = builder
                    .comment("Maximum size of the sphere")
                    .defineInRange("maxSphereSize", 28, 1, 100);

            minSphereSize = builder
                    .comment("Minimum size of the sphere")
                    .defineInRange("minSphereSize", 4, 1, 100);

            oreChance = builder
                    .comment("Chance of spawning ore (0.0 to 1.0)")
                    .defineInRange("oreChance", 0.005, 0.0, 1.0);

            blockDenyList = builder
                    .comment("List of blocks that will not be used for sphere generation. Separate with commas.")
                    .define("blockDenyList", "examplemod:example_block");
        }

        public static void loadConfig(ForgeConfigSpec config, Path path) {
            final CommentedFileConfig file = CommentedFileConfig.builder(path)
                    .sync()
                    .autosave()
                    .writingMode(WritingMode.REPLACE)
                    .build();
            file.load();
            config.setConfig(file);
        }
    }

    private static void initializeOreMap() {
        for (Biome biome : ForgeRegistries.BIOMES) {
            if (biome == null || biome.getRegistryName() == null) continue;
            if (!biome.getRegistryName().getPath().contains("nether") && !biome.getRegistryName().getPath().contains("end")) {
                List<BlockState> ores = new ArrayList<>();
                for (Block block : ForgeRegistries.BLOCKS) {
                    if (block != Blocks.AIR && block.getRegistryName() != null && block.getRegistryName().getPath().contains("ore") && !block.getRegistryName().getPath().contains("nether") && !block.getRegistryName().getPath().contains("end") && !blockDenyList.contains(block.getRegistryName().toString())) {
                        ores.add(block.getDefaultState());
                        logger.info("Added " + block.getRegistryName().getPath() + " to " + biome.getRegistryName().getPath());
                    }
                }
                BIOME_ORE_MAP.put(biome, ores);
                logger.info("Added " + ores.size() + " ores to " + biome.getRegistryName().getPath());
            }
        }
    }

    public static class SphereWorldGenerator {
        public void generate(WorldGenRegion region, Random random) {
            int chunkStartX = region.getMainChunkX();
            int chunkStartZ = region.getMainChunkZ();
            int chunkEndX = chunkStartX + 16;
            int chunkEndZ = chunkStartZ + 16;

            for (int chunkX = chunkStartX; chunkX < chunkEndX; chunkX++) {
                for (int chunkZ = chunkStartZ; chunkZ < chunkEndZ; chunkZ++) {
                    if (random.nextFloat() < SphereConfig.COMMON.density.get()) {
                        int x = chunkX * 16 + random.nextInt(16);
                        int z = chunkZ * 16 + random.nextInt(16);
                        int y = SphereConfig.COMMON.minHeight.get() + random.nextInt(SphereConfig.COMMON.maxHeight.get() - SphereConfig.COMMON.minHeight.get() + 1);
                        generateSphere(region, new BlockPos(x, y, z), random);
                    }
                }
            }
        }

        private void generateSphere(IWorld world, BlockPos pos, Random random) {
            Biome biome = world.getBiome(pos);
            BlockState state = getIBlockState(biome, random);

            int radius = SphereConfig.COMMON.minSphereSize.get() + random.nextInt(SphereConfig.COMMON.maxSphereSize.get() - SphereConfig.COMMON.minSphereSize.get() + 1);
            List<BlockState> ores = BIOME_ORE_MAP.getOrDefault(biome, Collections.emptyList());

            BlockPos.Mutable mutablePos = new BlockPos.Mutable();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radius * radius) {
                            mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                            if (!ores.isEmpty() && random.nextFloat() < SphereConfig.COMMON.oreChance.get()) {
                                BlockState oreState = ores.get(random.nextInt(ores.size()));
                                world.setBlockState(mutablePos, oreState, 2);
                            } else {
                                world.setBlockState(mutablePos, state, 2);
                            }
                        }
                    }
                }
            }
        }

        private static BlockState getIBlockState(Biome biome, Random random) {
            BlockState state = random.nextBoolean() ? biome.getGenerationSettings().getSurfaceBuilderConfig().getTop() : biome.getGenerationSettings().getSurfaceBuilderConfig().getUnder();

            if (isSpecialBlock(state)) {
                state = Blocks.STONE.getDefaultState();
            }
            if (blockDenyList.contains(Objects.requireNonNull(state.getBlock().getRegistryName()).toString())) {
                state = Blocks.STONE.getDefaultState();
            }
            return state;
        }

        static boolean isSpecialBlock(BlockState state) {
            return !state.getFluidState().isEmpty() || hasTileEntity(state.getBlock());
        }

        private static boolean hasTileEntity(Block block) {
            try {
                return block.hasTileEntity(block.getDefaultState());
            } catch (Exception e) {
                return false;
            }
        }
    }
}
