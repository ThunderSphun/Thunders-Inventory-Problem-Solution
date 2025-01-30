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


	public static final Map<Block, NibbleBlock> BLOCK_TO_NIBBLE = Map.of(Blocks.STONE, STONE_NIBBLE);

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, id("stone_nibble"), STONE_NIBBLE);
		Registry.register(Registries.ITEM, id("stone_nibble"), STONE_NIBBLE_ITEM);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(e -> e.addAfter(Items.STONE, STONE_NIBBLE_ITEM));

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