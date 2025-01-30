package com.github.thundersphun.mixin.client;

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
			if (NibbleBlock.playerNibbleMode(player)) {
				if (state.getBlock() instanceof NibbleBlock) {
					BooleanProperty hitNibble = NibbleBlock.getAttackHitNibble(hitPos, hitResult.getSide());

					VertexRendering.drawOutline(matrices, vertexConsumer,
							NibbleBlock.propertyToShape(hitNibble),
							offset.getX(), offset.getY(), offset.getZ(),
							color);
					return;
				}
				Vector3f min = new Vector3f(hitPos.getX() < 0.5f ? 0.0f : 0.5f, hitPos.getY() < 0.5f ? 0.0f : 0.5f, hitPos.getZ() < 0.5f ? 0.0f : 0.5f);
				Vector3f max = new Vector3f(hitPos.getX() < 0.5f ? 0.5f : 1.0f, hitPos.getY() < 0.5f ? 0.5f : 1.0f, hitPos.getZ() < 0.5f ? 0.5f : 1.0f);

				switch (hitResult.getSide()) {
					case DOWN -> {min.y = 0; max.y = 0;}
					case UP ->   {min.y = 1; max.y = 1;}
					case NORTH ->{min.z = 0; max.z = 0;}
					case SOUTH ->{min.z = 1; max.z = 1;}
					case WEST -> {min.x = 0; max.x = 0;}
					case EAST -> {min.x = 1; max.x = 1;}
				}

				VertexRendering.drawBox(matrices, vertexConsumer,
						min.x() + offset.getX(), min.y() + offset.getY(), min.z() + offset.getZ(),
						max.x() + offset.getX(), max.y() + offset.getY(), max.z() + offset.getZ(),
						ColorHelper.getRed(color), ColorHelper.getGreen(color), ColorHelper.getBlue(color), ColorHelper.getAlpha(color));
			}
		}
	}
}
