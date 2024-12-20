package info.burntrouter.skyspheres;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.logging.Logger;

@Mod(modid = SkySpheres.MODID, name = SkySpheres.NAME, version = SkySpheres.VERSION)
public class SkySpheres {
    public static final String MODID = "skyspheres";
    public static final String NAME = "Sky Spheres";
    public static final String VERSION = "1.0.6";

    private static final Map<Biome, List<IBlockState>> BIOME_ORE_MAP = new HashMap<>();

    private static final Logger logger = Logger.getLogger(SkySpheres.class.getName());
    private static final List<String> blockDenyList = Arrays.asList(SphereConfig.blockDenyList.split(","));

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        initializeOreMap();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            GameRegistry.registerWorldGenerator(new SphereWorldGenerator(), 0);
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }

    @Config(modid = MODID)
    public static class SphereConfig {
        @Config.Name("Minimum Height")
        @Config.Comment("Minimum height at which spheres can generate")
        public static int minHeight = 180;

        @Config.Name("Maximum Height")
        @Config.Comment("Maximum height at which spheres can generate")
        public static int maxHeight = 230;

        @Config.Name("Density")
        @Config.Comment("Density of sphere generation (0.0 to 1.0)")
        public static float density = 0.03f;

        @Config.Name("Max Sphere Size")
        @Config.Comment("Maximum size of the sphere")
        public static int maxSphereSize = 28;

        @Config.Name("Min Sphere Size")
        @Config.Comment("Minimum size of the sphere")
        public static int minSphereSize = 4;

        @Config.Name("Ore Spawn Chance")
        @Config.Comment("Chance of spawning ore (0.0 to 1.0)")
        public static float oreChance = 0.005f;

        @Config.Name("Block Deny List")
        @Config.Comment("List of blocks that will not be used for sphere generation. Separate with commas.")
        public static String blockDenyList = "examplemod:example_block";
    }

    private static void initializeOreMap() {
        for (Biome biome : Biome.REGISTRY) {
            if (biome == null || biome.getRegistryName() == null) continue;
            if (!biome.getRegistryName().getResourcePath().contains("nether") && !biome.getRegistryName().getResourcePath().contains("end")) {
                List<IBlockState> ores = new ArrayList<>();
                for (String oreName : OreDictionary.getOreNames()) {
                    if (oreName.startsWith("ore")) {
                        List<ItemStack> oreStacks = OreDictionary.getOres(oreName);
                        for (ItemStack stack : oreStacks) {
                            Block block = Block.getBlockFromItem(stack.getItem());
                            if (block != Blocks.AIR && block.getRegistryName() != null && !block.getRegistryName().getResourcePath().contains("nether") && !block.getRegistryName().getResourcePath().contains("end") && !blockDenyList.contains(block.getRegistryName().toString())) {
                                ores.add(block.getDefaultState());
                                logger.info("Added " + block.getRegistryName().getResourcePath() + " to " + biome.getRegistryName().getResourcePath());
                            }
                        }
                    }
                }
                BIOME_ORE_MAP.put(biome, ores);
                logger.info("Added " + ores.size() + " ores to " + biome.getRegistryName().getResourcePath());
            }
        }
    }

    public static class SphereWorldGenerator implements IWorldGenerator {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            if (world.provider.getDimension() == 0 && random.nextFloat() < SphereConfig.density) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int y = SphereConfig.minHeight + random.nextInt(SphereConfig.maxHeight - SphereConfig.minHeight + 1);
                generateSphere(world, new BlockPos(x, y, z), random);
            }
        }

        private void generateSphere(World world, BlockPos pos, Random random) {
            Biome biome = world.getBiome(pos);
            IBlockState state = getIBlockState(biome, random);

            int radius = SphereConfig.minSphereSize + random.nextInt(SphereConfig.maxSphereSize - SphereConfig.minSphereSize + 1);
            List<IBlockState> ores = BIOME_ORE_MAP.getOrDefault(biome, Collections.emptyList());

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radius * radius) {
                            mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                            if (!ores.isEmpty() && random.nextFloat() < SphereConfig.oreChance) {
                                IBlockState oreState = ores.get(random.nextInt(ores.size()));
                                world.setBlockState(mutablePos, oreState, 2);
                            } else {
                                world.setBlockState(mutablePos, state, 2);
                            }
                        }
                    }
                }
            }
        }

        private static IBlockState getIBlockState(Biome biome, Random random) {
            IBlockState state = random.nextBoolean() ? biome.topBlock : biome.fillerBlock;

            if (isSpecialBlock(state.getBlock())) {
                //If the block is special just use stone
                state = Blocks.STONE.getDefaultState();
            }
            if (blockDenyList.contains(state.getBlock().getRegistryName())) {
                state = Blocks.STONE.getDefaultState();
            }
            return state;
        }

        static boolean isSpecialBlock(Block block) {
            return block instanceof BlockFalling || block instanceof BlockLiquid || block.canProvidePower(block.getDefaultState()) || hasTileEntity(block);
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
