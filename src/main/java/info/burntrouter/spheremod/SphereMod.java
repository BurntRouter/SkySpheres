package info.burntrouter.spheremod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mod(modid = SphereMod.MODID, name = SphereMod.NAME, version = SphereMod.VERSION)
public class SphereMod {
    public static final String MODID = "spheremod";
    public static final String NAME = "Sphere Mod";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
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
        public static int maxHeight = 255;

        @Config.Name("Density")
        @Config.Comment("Density of sphere generation (0.0 to 1.0)")
        public static float density = 0.1f;
    }

    public static class SphereWorldGenerator implements IWorldGenerator {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            if (world.provider.getDimension() == 0) { // Only generate in the Overworld
                if (random.nextFloat() < SphereConfig.density) {
                    int x = chunkX * 16 + random.nextInt(16);
                    int z = chunkZ * 16 + random.nextInt(16);
                    int y = SphereConfig.minHeight + random.nextInt(SphereConfig.maxHeight - SphereConfig.minHeight);
                    generateSphere(world, new BlockPos(x, y, z), random);
                }
            }
        }

        private void generateSphere(World world, BlockPos pos, Random random) {
            Biome biome = world.getBiome(pos);
            IBlockState state = getIBlockState(biome);

            int radius = 4 + random.nextInt(4); // Random radius between 4 and 7
            Set<BlockPos> positions = getBlockPos(pos, radius);

            // Set blocks in the world
            for (BlockPos newPos : positions) {
                world.setBlockState(newPos, state, 2);
            }
        }

        private static Set<BlockPos> getBlockPos(BlockPos pos, int radius) {
            Set<BlockPos> positions = new HashSet<>();

            // Calculate sphere positions to optimize block placement
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radius * radius) {
                            positions.add(pos.add(x, y, z));
                        }
                    }
                }
            }
            return positions;
        }

        private static IBlockState getIBlockState(Biome biome) {
            IBlockState state = biome.topBlock;

            if (state == Blocks.AIR.getDefaultState() || state == Blocks.WATER.getDefaultState() || state == Blocks.LAVA.getDefaultState()) {
                state = Blocks.DIRT.getDefaultState(); // Fallback in case biome top block is air
            }

            if (state == Blocks.SAND.getDefaultState()) {
                state = Blocks.SANDSTONE.getDefaultState(); // Fallback in case biome top block is sand
            }

            if (state == Blocks.GRAVEL.getDefaultState()) {
                state = Blocks.STONE.getDefaultState(); // Fallback in case biome top block is gravel
            }
            return state;
        }
    }
}
