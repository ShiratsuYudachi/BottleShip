package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.*;
public class StructurePlacer {
	public static void placeStructure(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos,
			String templateName) {
		StructureTemplateManager templateManager = Objects.requireNonNull(serverLevelAccessor.getServer())
				.getStructureManager();
		Optional<StructureTemplate> templateOptional = templateManager.get(new ResourceLocation(
				"C:/Users/ForgeStove/Desktop/nbt/jidi.nbt"));
		if (templateOptional.isPresent()) {
			StructureTemplate structureTemplate = templateOptional.get();
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setIgnoreEntities(false);
			structureTemplate.placeInWorld(
					serverLevelAccessor,
					blockPos,
					blockPos,
					structurePlaceSettings,
					serverLevelAccessor.getRandom(),
					2
			);
		}
	}
}
