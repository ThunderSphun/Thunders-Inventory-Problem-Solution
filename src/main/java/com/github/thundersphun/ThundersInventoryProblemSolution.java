package com.github.thundersphun;

import com.github.thundersphun.block.NibbleBlock;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ThundersInventoryProblemSolution implements ModInitializer {
	public static final String MOD_ID = "tips";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final NibbleBlock STONE_NIBBLE = new NibbleBlock(
			Blocks.STONE,
			AbstractBlock.Settings
					.copy(Blocks.STONE)
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id("stone_nibble"))));
	public static final BlockItem STONE_NIBBLE_ITEM = new BlockItem(
			STONE_NIBBLE,
			new Item.Settings()
					.useBlockPrefixedTranslationKey()
					.registryKey(RegistryKey.of(RegistryKeys.ITEM, id("stone_nibble"))));

	public static final HammerItem WOODEN_HAMMER = new HammerItem(
			new ToolMaterial(
					ToolMaterial.WOOD.incorrectBlocksForDrops(),
					ToolMaterial.WOOD.durability() * 4,
					ToolMaterial.WOOD.speed(),
					ToolMaterial.WOOD.attackDamageBonus(),
					ToolMaterial.WOOD.enchantmentValue(),
					ToolMaterial.WOOD.repairItems()),
			BlockTags.PICKAXE_MINEABLE,
			7f,
			-3.2f,
			new Item.Settings()
					.registryKey(RegistryKey.of(RegistryKeys.ITEM, id("wooden_hammer"))));

	public static final Map<Block, NibbleBlock> BLOCK_TO_NIBBLE = Map.of(Blocks.STONE, STONE_NIBBLE);

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, id("stone_nibble"), STONE_NIBBLE);
		Registry.register(Registries.ITEM, id("stone_nibble"), STONE_NIBBLE_ITEM);

		Registry.register(Registries.ITEM, id("wooden_hammer"), WOODEN_HAMMER);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(e -> e.addAfter(Items.STONE, STONE_NIBBLE_ITEM));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.addAfter(Items.WOODEN_HOE, WOODEN_HAMMER));

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (NibbleBlock.playerNibbleMode(player)) {
				NibbleBlock block = BLOCK_TO_NIBBLE.get(state.getBlock());
				if (block != null) {
					state = block.breakNibble(world, pos, block.getFullState(), player);
					world.setBlockState(pos, state, Block.NOTIFY_NEIGHBORS);
					return false;
				}
			}
			return true;
		});
	}

	public static Identifier id(String s) {
		return Identifier.of(MOD_ID, s);
	}
}