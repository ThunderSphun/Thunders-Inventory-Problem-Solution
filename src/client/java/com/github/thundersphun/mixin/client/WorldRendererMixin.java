package com.github.thundersphun.mixin.client;

import com.github.thundersphun.HammerItem;
import com.github.thundersphun.ThundersInventoryProblemSolution;
import com.github.thundersphun.block.NibbleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements SynchronousResourceReloader, AutoCloseable {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V",
			at = @At("HEAD"))
	private void drawBlockOutline(
			MatrixStack matrices,
			VertexConsumer vertexConsumer,
			Entity entity,
			double cameraX,
			double cameraY,
			double cameraZ,
			BlockPos pos,
			BlockState state,
			int color,
			CallbackInfo ci) {
		if (entity instanceof PlayerEntity player) {
			BlockHitResult hitResult = (BlockHitResult) this.client.crosshairTarget;
			Vec3d hitPos = hitResult.getPos().subtract(Vec3d.of(pos));
			Vec3d offset = Vec3d.of(pos).subtract(cameraX, cameraY, cameraZ);
			if (player.getStackInHand(player.getActiveHand()).getItem() instanceof HammerItem) {
				if (!ThundersInventoryProblemSolution.BLOCK_TO_NIBBLE.containsKey(state.getBlock())) {
					return;
				}
				BooleanProperty hitNibble = NibbleBlock.getAttackHitNibble(hitPos, hitResult.getSide());

				VertexRendering.drawOutline(matrices, vertexConsumer,
						NibbleBlock.propertyToShape(hitNibble),
						offset.getX(), offset.getY(), offset.getZ(),
						color);
			} else if (NibbleBlock.playerNibbleMode(player)) {
				BooleanProperty hitNibble = NibbleBlock.getAttackHitNibble(hitPos, hitResult.getSide());

				VertexRendering.drawOutline(matrices, vertexConsumer,
						NibbleBlock.propertyToShape(hitNibble),
						offset.getX(), offset.getY(), offset.getZ(),
						color);
			}
		}
	}
}
