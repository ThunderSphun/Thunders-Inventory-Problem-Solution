package com.github.thundersphun.block;

import com.github.thundersphun.HammerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class NibbleBlock extends Block implements Waterloggable {
	public static final BooleanProperty WDN = BooleanProperty.of("west_down_north");
	public static final BooleanProperty WDS = BooleanProperty.of("west_down_south");
	public static final BooleanProperty WUN = BooleanProperty.of("west_up_north");
	public static final BooleanProperty WUS = BooleanProperty.of("west_up_south");
	public static final BooleanProperty EDN = BooleanProperty.of("east_down_north");
	public static final BooleanProperty EDS = BooleanProperty.of("east_down_south");
	public static final BooleanProperty EUN = BooleanProperty.of("east_up_north");
	public static final BooleanProperty EUS = BooleanProperty.of("east_up_south");

	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private static final VoxelShape WDN_SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5);
	private static final VoxelShape WDS_SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.5, 0.5, 0.5, 1.0);
	private static final VoxelShape WUN_SHAPE = VoxelShapes.cuboid(0.0, 0.5, 0.0, 0.5, 1.0, 0.5);
	private static final VoxelShape WUS_SHAPE = VoxelShapes.cuboid(0.0, 0.5, 0.5, 0.5, 1.0, 1.0);
	private static final VoxelShape EDN_SHAPE = VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 0.5, 0.5);
	private static final VoxelShape EDS_SHAPE = VoxelShapes.cuboid(0.5, 0.0, 0.5, 1.0, 0.5, 1.0);
	private static final VoxelShape EUN_SHAPE = VoxelShapes.cuboid(0.5, 0.5, 0.0, 1.0, 1.0, 0.5);
	private static final VoxelShape EUS_SHAPE = VoxelShapes.cuboid(0.5, 0.5, 0.5, 1.0, 1.0, 1.0);

	private final Block baseBlock;

	public NibbleBlock(Block baseBlock, Settings settings) {
		super(settings);
		this.baseBlock = baseBlock;

		setDefaultState(getEmptyState().with(WATERLOGGED, false));
	}

	public static VoxelShape propertyToShape(BooleanProperty hitNibble) {
		if (hitNibble == WDN) return WDN_SHAPE;
		if (hitNibble == WDS) return WDS_SHAPE;
		if (hitNibble == WUN) return WUN_SHAPE;
		if (hitNibble == WUS) return WUS_SHAPE;
		if (hitNibble == EDN) return EDN_SHAPE;
		if (hitNibble == EDS) return EDS_SHAPE;
		if (hitNibble == EUN) return EUN_SHAPE;
		if (hitNibble == EUS) return EUS_SHAPE;

		return VoxelShapes.empty();
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED)
				.add(WDN).add(WDS).add(WUN).add(WUS)
				.add(EDN).add(EDS).add(EUN).add(EUS);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		VoxelShape shape = VoxelShapes.empty();

		if (state.get(WDN)) shape = VoxelShapes.combine(shape, WDN_SHAPE, BooleanBiFunction.OR);
		if (state.get(WDS)) shape = VoxelShapes.combine(shape, WDS_SHAPE, BooleanBiFunction.OR);
		if (state.get(WUN)) shape = VoxelShapes.combine(shape, WUN_SHAPE, BooleanBiFunction.OR);
		if (state.get(WUS)) shape = VoxelShapes.combine(shape, WUS_SHAPE, BooleanBiFunction.OR);
		if (state.get(EDN)) shape = VoxelShapes.combine(shape, EDN_SHAPE, BooleanBiFunction.OR);
		if (state.get(EDS)) shape = VoxelShapes.combine(shape, EDS_SHAPE, BooleanBiFunction.OR);
		if (state.get(EUN)) shape = VoxelShapes.combine(shape, EUN_SHAPE, BooleanBiFunction.OR);
		if (state.get(EUS)) shape = VoxelShapes.combine(shape, EUS_SHAPE, BooleanBiFunction.OR);

		return shape.simplify();
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (isFull(state)) {
			world.setBlockState(pos, this.baseBlock.getDefaultState());
		} else if (isEmpty(state)) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();
		BlockState state = world.getBlockState(pos);

		BlockState nextState = null;
		if (state.isOf(this) && ((NibbleBlock) state.getBlock()).baseBlock == this.baseBlock) {
			nextState = state;
		} else if (state.canReplace(ctx)) {
			nextState = getDefaultState();
		}
		if (nextState != null) {
			nextState = nextState
					.with(WATERLOGGED, world.getFluidState(pos).isOf(Fluids.WATER))
					.with(getPlaceHitNibble(ctx.getHitPos().subtract(Vec3d.of(pos)), ctx.getSide()), true);
		}

		return nextState;
	}

	protected boolean canReplace(BlockState state, ItemPlacementContext context) {
		ItemStack stack = context.getStack();
		if (!stack.isOf(asItem())) {
			return isEmpty(state);
		}
		if (stack.contains(DataComponentTypes.BLOCK_STATE)) {
			return false;
		}

		BlockPos pos = context.getBlockPos();
		Vec3d hitPos = context.getHitPos().subtract(Vec3d.of(pos));
		Block block = state.getBlock();
		if (block instanceof NibbleBlock nibbleBlock && nibbleBlock.baseBlock == this.baseBlock) {
			Direction side = context.getSide();
			if (side == Direction.WEST	&& hitPos.getX() == 0) return false;
			if (side == Direction.EAST	&& hitPos.getX() == 1) return false;
			if (side == Direction.DOWN	&& hitPos.getY() == 0) return false;
			if (side == Direction.UP	&& hitPos.getY() == 1) return false;
			if (side == Direction.NORTH	&& hitPos.getZ() == 0) return false;
			if (side == Direction.SOUTH	&& hitPos.getZ() == 1) return false;
			return true;
		}

		return isEmpty(state);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(
			BlockState state,
			WorldView world,
			ScheduledTickView tickView,
			BlockPos pos,
			Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			Random random) {
		if (state.get(WATERLOGGED)) {
			tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		ItemStack stack = super.getPickStack(world, pos, state, includeData);
		if (includeData) {
			stack.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT
							.with(WDN, state.get(WDN)).with(WDS, state.get(WDS))
							.with(WUN, state.get(WUN)).with(WUS, state.get(WUS))
							.with(EDN, state.get(EDN)).with(EDS, state.get(EDS))
							.with(EUN, state.get(EUN)).with(EUS, state.get(EUS)));
		}
		return stack;
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (playerNibbleMode(player)) {
			state = breakNibble(world, pos, state, player);
		} else {
			super.onBreak(world, pos, state, player);
			state = getEmptyState().with(WATERLOGGED, state.get(WATERLOGGED));
		}

		return state;
	}

	public BlockState breakNibble(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		HitResult hit = player.raycast(player.getBlockInteractionRange(), 1.0f, false);

		BooleanProperty hitNibble = getAttackHitNibble(hit.getPos().subtract(Vec3d.of(pos)), ((BlockHitResult) hit).getSide());

		return state.with(hitNibble, false);
	}

	@Override
	public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
		if (isEmpty(state)) {
			state = state.get(WATERLOGGED) ? Fluids.WATER.getDefaultState().getBlockState() : Blocks.AIR.getDefaultState();
		}
		world.setBlockState(pos, state, Block.NOTIFY_NEIGHBORS);
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return switch (rotation) {
			case NONE -> state;
			case CLOCKWISE_90 -> state
					.with(WDN, state.get(EDN)).with(WDS, state.get(WDN))
					.with(WUN, state.get(EUN)).with(WUS, state.get(WUN))
					.with(EDN, state.get(EDS)).with(EDS, state.get(WDS))
					.with(EUN, state.get(EUS)).with(EUS, state.get(WUS));
			case CLOCKWISE_180 -> state
					.with(WDN, state.get(EDS)).with(WDS, state.get(EDN))
					.with(WUN, state.get(EUS)).with(WUS, state.get(EUN))
					.with(EDN, state.get(WDS)).with(EDS, state.get(WDN))
					.with(EUN, state.get(WUS)).with(EUS, state.get(WUN));
			case COUNTERCLOCKWISE_90 -> state
					.with(WDN, state.get(WDS)).with(WDS, state.get(EDS))
					.with(WUN, state.get(WUS)).with(WUS, state.get(EUS))
					.with(EDN, state.get(WDN)).with(EDS, state.get(EDN))
					.with(EUN, state.get(WUN)).with(EUS, state.get(EUN));
		};
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return switch (mirror) {
			case NONE -> state;
			case LEFT_RIGHT -> state
					.with(WDN, state.get(WDS)).with(WDS, state.get(WDN))
					.with(WUN, state.get(WUS)).with(WUS, state.get(WUN))
					.with(EDN, state.get(EDS)).with(EDS, state.get(EDN))
					.with(EUN, state.get(EUS)).with(EUS, state.get(EUN));
			case FRONT_BACK -> state
					.with(WDN, state.get(EDN)).with(WDS, state.get(EDS))
					.with(WUN, state.get(EUN)).with(WUS, state.get(EUS))
					.with(EDN, state.get(WDN)).with(EDS, state.get(WDS))
					.with(EUN, state.get(WUN)).with(EUS, state.get(WUS));
		};
	}

	private boolean isFull(BlockState state) {
		if (!state.get(WDN)) return false;
		if (!state.get(WDS)) return false;
		if (!state.get(WUN)) return false;
		if (!state.get(WUS)) return false;
		if (!state.get(EDN)) return false;
		if (!state.get(EDS)) return false;
		if (!state.get(EUN)) return false;
		if (!state.get(EUS)) return false;

		return true;
	}

	public BlockState getFullState() {
		return getDefaultState()
				.with(WDN, true).with(WDS, true).with(WUN, true).with(WUS, true)
				.with(EDN, true).with(EDS, true).with(EUN, true).with(EUS, true);
	}

	private boolean isEmpty(BlockState state) {
		if (state.get(WDN)) return false;
		if (state.get(WDS)) return false;
		if (state.get(WUN)) return false;
		if (state.get(WUS)) return false;
		if (state.get(EDN)) return false;
		if (state.get(EDS)) return false;
		if (state.get(EUN)) return false;
		if (state.get(EUS)) return false;

		return true;
	}

	public BlockState getEmptyState() {
		return getDefaultState()
				.with(WDN, false).with(WDS, false).with(WUN, false).with(WUS, false)
				.with(EDN, false).with(EDS, false).with(EUN, false).with(EUS, false);
	}

	public static boolean playerNibbleMode(PlayerEntity player) {
		ItemStack hand = player.getStackInHand(player.getActiveHand());
		return hand.getItem() instanceof HammerItem ||
				(player.isCreative() && hand.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof NibbleBlock);
	}

	public static BooleanProperty getPlaceHitNibble(Vec3d pos, Direction face) {
		if (MathHelper.approximatelyEquals(pos.getX(), 0.5)) {
			if (pos.getY() < 0.5 && pos.getZ() < 0.5) return face == Direction.WEST ? WDN : EDN;
			if (pos.getY() < 0.5 && pos.getZ() > 0.5) return face == Direction.WEST ? WDS : EDS;
			if (pos.getY() > 0.5 && pos.getZ() < 0.5) return face == Direction.WEST ? WUN : EUN;
			if (pos.getY() > 0.5 && pos.getZ() > 0.5) return face == Direction.WEST ? WUS : EUS;
		}

		if (MathHelper.approximatelyEquals(pos.getY(), 0.5)) {
			if (pos.getX() < 0.5 && pos.getZ() < 0.5) return face == Direction.DOWN ? WDN : WUN;
			if (pos.getX() < 0.5 && pos.getZ() > 0.5) return face == Direction.DOWN ? WDS : WUS;
			if (pos.getX() > 0.5 && pos.getZ() < 0.5) return face == Direction.DOWN ? EDN : EUN;
			if (pos.getX() > 0.5 && pos.getZ() > 0.5) return face == Direction.DOWN ? EDS : EUS;
		}

		if (MathHelper.approximatelyEquals(pos.getZ(), 0.5)) {
			if (pos.getX() < 0.5 && pos.getY() < 0.5) return face == Direction.NORTH ? WDN : WDS;
			if (pos.getX() > 0.5 && pos.getY() < 0.5) return face == Direction.NORTH ? EDN : EDS;
			if (pos.getX() < 0.5 && pos.getY() > 0.5) return face == Direction.NORTH ? WUN : WUS;
			if (pos.getX() > 0.5 && pos.getY() > 0.5) return face == Direction.NORTH ? EUN : EUS;
		}

		if (pos.getX() < 0.5 && pos.getY() < 0.5 && pos.getZ() < 0.5) return WDN;
		if (pos.getX() < 0.5 && pos.getY() < 0.5 && pos.getZ() > 0.5) return WDS;
		if (pos.getX() < 0.5 && pos.getY() > 0.5 && pos.getZ() < 0.5) return WUN;
		if (pos.getX() < 0.5 && pos.getY() > 0.5 && pos.getZ() > 0.5) return WUS;
		if (pos.getX() > 0.5 && pos.getY() < 0.5 && pos.getZ() < 0.5) return EDN;
		if (pos.getX() > 0.5 && pos.getY() < 0.5 && pos.getZ() > 0.5) return EDS;
		if (pos.getX() > 0.5 && pos.getY() > 0.5 && pos.getZ() < 0.5) return EUN;
		if (pos.getX() > 0.5 && pos.getY() > 0.5 && pos.getZ() > 0.5) return EUS;

		return WDN;
	}

	public static BooleanProperty getAttackHitNibble(Vec3d pos, Direction face) {
		if (MathHelper.approximatelyEquals(pos.getX(), 0.5)) {
			if (pos.getY() < 0.5 && pos.getZ() < 0.5) return face == Direction.WEST ? EDN : WDN;
			if (pos.getY() < 0.5 && pos.getZ() > 0.5) return face == Direction.WEST ? EDS : WDS;
			if (pos.getY() > 0.5 && pos.getZ() < 0.5) return face == Direction.WEST ? EUN : WUN;
			if (pos.getY() > 0.5 && pos.getZ() > 0.5) return face == Direction.WEST ? EUS : WUS;
		}

		if (MathHelper.approximatelyEquals(pos.getY(), 0.5)) {
			if (pos.getX() < 0.5&& pos.getZ() < 0.5) return face == Direction.DOWN ? WUN : WDN;
			if (pos.getX() < 0.5&& pos.getZ() > 0.5) return face == Direction.DOWN ? WUS : WDS;
			if (pos.getX() > 0.5&& pos.getZ() < 0.5) return face == Direction.DOWN ? EUN : EDN;
			if (pos.getX() > 0.5&& pos.getZ() > 0.5) return face == Direction.DOWN ? EUS : EDS;
		}

		if (MathHelper.approximatelyEquals(pos.getZ(), 0.5)) {
			if (pos.getX() < 0.5 && pos.getY() < 0.5) return face == Direction.NORTH ? WDS : WDN;
			if (pos.getX() > 0.5 && pos.getY() < 0.5) return face == Direction.NORTH ? EDS : EDN;
			if (pos.getX() < 0.5 && pos.getY() > 0.5) return face == Direction.NORTH ? WUS : WUN;
			if (pos.getX() > 0.5 && pos.getY() > 0.5) return face == Direction.NORTH ? EUS : EUN;
		}

		if (pos.getX() < 0.5 && pos.getY() < 0.5 && pos.getZ() < 0.5) return WDN;
		if (pos.getX() < 0.5 && pos.getY() < 0.5 && pos.getZ() > 0.5) return WDS;
		if (pos.getX() < 0.5 && pos.getY() > 0.5 && pos.getZ() < 0.5) return WUN;
		if (pos.getX() < 0.5 && pos.getY() > 0.5 && pos.getZ() > 0.5) return WUS;
		if (pos.getX() > 0.5 && pos.getY() < 0.5 && pos.getZ() < 0.5) return EDN;
		if (pos.getX() > 0.5 && pos.getY() < 0.5 && pos.getZ() > 0.5) return EDS;
		if (pos.getX() > 0.5 && pos.getY() > 0.5 && pos.getZ() < 0.5) return EUN;
		if (pos.getX() > 0.5 && pos.getY() > 0.5 && pos.getZ() > 0.5) return EUS;

		return WDN;
	}
}
