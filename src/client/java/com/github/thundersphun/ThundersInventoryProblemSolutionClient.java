package com.github.thundersphun;

import com.github.thundersphun.block.NibbleBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.block.Block;

public class ThundersInventoryProblemSolutionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if (NibbleBlock.playerNibbleMode(player)) {
				NibbleBlock block = ThundersInventoryProblemSolution.BLOCK_TO_NIBBLE.get(state.getBlock());
				if (block != null) {
					state = block.breakNibble(world, pos, block.getFullState(), player);
					world.setBlockState(pos, state, Block.NOTIFY_NEIGHBORS);
				}
			}
		});
	}
}
