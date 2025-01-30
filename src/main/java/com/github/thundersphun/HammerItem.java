package com.github.thundersphun;

import com.github.thundersphun.block.NibbleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HammerItem extends MiningToolItem {
	public HammerItem(ToolMaterial material, TagKey<Block> effectiveBlocks, float attackDamage, float attackSpeed, Settings settings) {
		super(material, effectiveBlocks, attackDamage, attackSpeed, settings);
	}

	@Override
	public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
		return ThundersInventoryProblemSolution.BLOCK_TO_NIBBLE.containsKey(state.getBlock()) || state.getBlock() instanceof NibbleBlock;
	}
}
