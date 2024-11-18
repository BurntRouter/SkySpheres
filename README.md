# Sky Spheres Mod

Sky Spheres is a Minecraft mod that generates floating spherical structures made of biome-specific materials. These spheres spawn in the sky and can contain ores and other resources, providing a unique exploration and mining experience.

## Features
- **Biome-Specific Spheres**: Generates spheres in the sky with blocks matching the biome they are found in.
- **Ore Generation**: Each sphere has a chance of containing ores, giving players additional incentive to explore.
- **Customizable Settings**: Adjust sphere size, height, density, and ore spawn rates through the configuration file.
- **Overworld Only**: The spheres are only generated in the overworld.

## Configuration
The mod includes several configurable settings that can be customized to suit your preferences. These settings can be modified in the configuration file:
- **Minimum Height** (`minHeight`): Minimum height at which spheres can generate (default: 180).
- **Maximum Height** (`maxHeight`): Maximum height at which spheres can generate (default: 230).
- **Density** (`density`): The probability of sphere generation per chunk (0.0 to 1.0, default: 0.03).
- **Maximum Sphere Size** (`maxSphereSize`): Maximum radius of the spheres (default: 28).
- **Minimum Sphere Size** (`minSphereSize`): Minimum radius of the spheres (default: 4).
- **Ore Spawn Chance** (`oreChance`): Chance of spawning ores within a sphere (0.0 to 1.0, default: 0.005).

## Installation
1. Install [Minecraft Forge](https://files.minecraftforge.net/).
2. Download the latest version of the Sky Spheres mod from the [releases page](#).
3. Place the downloaded `.jar` file into your Minecraft `mods` folder.
4. Launch Minecraft with the Forge profile to enjoy the mod.

## Compatibility
- **Minecraft Version**: 1.12.2
- **Dependencies**: Requires Minecraft Forge.
- **Mod Compatibility**: Compatible with most mods that do not alter the overworld generation drastically. I tested this in Enigmatica 2: Expert and it worked fine.

## Known Issues
- Ore spawns are not biome-specific and are randomly distributed within the spheres. This means ores for specific biomes or dimensions may be randomly generated in any sphere.
- Spheres may generate fully with grass blocks due to the way the sphere generation algorithm works. This may be addressed in future updates.

## License
This mod is open-source and licensed under the [MIT License](https://github.com/BurntRouter/SkySpheres/blob/main/LICENSE). Contributions are welcome!

## Credits
- **Author**: BurntRouter (Me!)
- **Contributors**: Shoutout to jdf2 for demanding we have spheres.

## Contributing
If you wish to contribute to the development of Sky Spheres, please feel free to submit a pull request or report any issues on the [GitHub repository](https://github.com/BurntRouter/SkySpheres/issues).

## Feedback & Support
If you encounter any issues or have suggestions for improvements, please open an issue on GitHub or contact me through Discord @ Router .
