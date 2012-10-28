package mod_StoneBreaker.client;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import mod_StoneBreaker.Config;
import mod_StoneBreaker.StoneBreaker;
import mod_StoneBreaker.Config.Mode;
import mod_StoneBreaker.EnumPacketType;
import mod_StoneBreaker.Position;
import mod_StoneBreaker.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemTool;
import net.minecraft.src.ModLoader;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler {
	private int prev_blockHitWait = 0;
	private Position prev_pos = new Position(0,0,0);
	private int prev_sideHit = 0;
	private int prev_blockId = 0;
	private int prev_metadata = 0;
	//private boolean debugmode = true;
	public static Set<Position> vectors = new LinkedHashSet();
	public static Set<Position> positions = new LinkedHashSet();
	private int breaking_blockId = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
			if (guiscreen == null) {
				onTickInGame();
			}
		}
	}

	private void onTickInGame() {
		checkPlayerBreak();

		Minecraft mc = ModLoader.getMinecraftInstance();

		if (mc.objectMouseOver == null) {
			return;
		}

		if (mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			int i = mc.objectMouseOver.blockX;
			int j = mc.objectMouseOver.blockY;
			int k = mc.objectMouseOver.blockZ;
			prev_pos.set(i, j, k);
			prev_sideHit = mc.objectMouseOver.sideHit;
			prev_blockId = mc.theWorld.getBlockId(i, j, k);
			Block block = Block.blocksList[prev_blockId];
			prev_metadata = mc.theWorld.getBlockMetadata(i, j, k);
			return;
		}
	}

	private void checkPlayerBreak() {
		int blockHitWait = Util.getBlockHitWait();
		if(blockHitWait == -1) {
			return;
		}
		boolean breakflag = (blockHitWait == 5 && blockHitWait != prev_blockHitWait);
		prev_blockHitWait = blockHitWait;

		if(breakflag) {
			breaking_blockId = prev_blockId;
			String str = "Block ID=" + prev_blockId + " Class=" + Block.blocksList[prev_blockId].getClass().getName();
			Util.debugPrintChatMessage(str);
		}

		Minecraft mc = ModLoader.getMinecraftInstance();
		ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
		if(itemStack == null) {
			positions.clear();
			return;
		}

		Item item = Item.itemsList[itemStack.itemID];
		if(item == null) {
			positions.clear();
			return;
		}

		if(item instanceof ItemTool == false && StoneBreaker.config.getTools().contains(item.getClass()) == false) {
			positions.clear();
			return;
		}

		if(breakflag) {
			positions.clear();

			if(StoneBreaker.config.getTarget().contains(prev_blockId) == false) {
				String str = prev_blockId + "not included in " + StoneBreaker.config.getTarget().toString();
				Util.debugPrintChatMessage(str);
				return;
			}

			setVector(StoneBreaker.config.mode);
			for(Position pos : initNextBreak(prev_pos, prev_blockId, StoneBreaker.config.mode)) {
				positions.add(pos);
				for(Position pos2 : getNextBreak(pos, prev_blockId)) {
					positions.add(pos2);
				}
			}
		}

		continueBreak(prev_blockId);
	}

	private void continueBreak(int blockId) {
		Set<Position> oldPosition = new LinkedHashSet();
		if(positions.isEmpty() == false) {
			oldPosition.addAll(positions);
			positions.clear();
		}

		int n = 0;
		for(Position pos : oldPosition) {
			Util.debugPrintChatMessage("continueBreak " + pos.toString());
			if(n < 16) {
				Minecraft mc = ModLoader.getMinecraftInstance();
				String playerName = mc.thePlayer.getEntityName();
				int targetId = mc.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
				if(breaking_blockId == targetId ||
						(breaking_blockId == Block.dirt.blockID && targetId == Block.grass.blockID) ||
						(breaking_blockId == Block.grass.blockID && targetId == Block.dirt.blockID)) {
					int metadata = mc.theWorld.getBlockMetadata((int)pos.x, (int)pos.y, (int)pos.z);

					try {
						Util.sendPacket(EnumPacketType.destroy.ordinal(), new String[]{playerName}, new int[]{pos.x, pos.y, pos.z, 0, breaking_blockId, metadata, StoneBreaker.config.drop_here ? 1 : 0});
						//Util.debugPrintChatMessage("continueBreak " + pos.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					for(Position nextPos : getNextBreak(pos, breaking_blockId)) {
						Util.debugPrintChatMessage("nextPos " + pos.toString());
						positions.add(nextPos);
					}
				}
			}
			else {
				positions.add(pos);
			}
			n++;
		}
	}

	public Set<Position> getNextBreak(Position prev_pos, int blockId) {
		Set<Position> newPositions = new LinkedHashSet();
		for(Position vector : vectors) {
			Position pos = new Position(prev_pos);
			pos.add(vector);
			Minecraft mc = ModLoader.getMinecraftInstance();
			int id = mc.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
			boolean bSame = true;
			if(StoneBreaker.config.getTarget().contains(id) == false) {
				String str = String.valueOf(id) + " not in " + StoneBreaker.config.getTarget().toString();
				Util.debugPrintChatMessage(str);
				bSame = false;
			}
			if(StoneBreaker.config.getTarget().contains(blockId) == false) {
				String str = String.valueOf(blockId) + " not in " + StoneBreaker.config.getTarget().toString();
				Util.debugPrintChatMessage(str);
				bSame = false;
			}
			if(StoneBreaker.config.mode == Mode.off) {
				bSame = false;
			}


			if(bSame) {
				newPositions.add(pos);
			}
		}
		return newPositions;
	}

	private Set<Position> initNextBreak(Position position, int blockId, Mode mode) {
		Set<Position> newPositions = new LinkedHashSet();
		switch(mode) {
		case off:
			break;
		case line:
			newPositions.add(new Position(position.x, position.y, position.z));
			break;
		case tunnel:
		case downstair:
		case upstair:
			newPositions.add(new Position(position.x, position.y, position.z));
			newPositions.add(new Position(position.x, position.y + 1, position.z));
			newPositions.add(new Position(position.x, position.y + 2, position.z));
			break;
		case upper:
		case under:
		case horizontal:
		case vertical:
		case all:
			newPositions.add(new Position(position.x, position.y, position.z));
			break;
		}

		return newPositions;
	}

	private void setVector(Mode mode) {

		Set<Position> backDirections = getBackDirections(prev_sideHit);
		vectors.clear();
		Position v;

		switch(mode) {
		case off:
			break;
		case line:
		case tunnel:
			vectors.add(getDirection(prev_sideHit));
			break;
		case downstair:
			switch(prev_sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				v = getDirection(prev_sideHit);
				v.y = -1;
				vectors.add(v);
				break;
			}
			break;
		case upstair:
			switch(prev_sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				v = getDirection(prev_sideHit);
				v.y = 1;
				vectors.add(v);
				break;
			}
			break;
		case upper:
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			switch(prev_sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				vectors.removeAll(backDirections);
				break;
			}

			break;
		case under:
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			switch(prev_sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				vectors.removeAll(backDirections);
				break;
			}
			break;
		case horizontal:
			vectors.add(new Position(1, 0, 0));vectors.add(new Position(-1, 0, 0));
			vectors.add(new Position(0, 0, 1));vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, -1));
			break;
		case vertical:

			/*
			 * 2 = (0, 0, 1)
			 * 3 = (0, 0, -1)
			 * 4 = (1, 0, 0)
			 * 5 = (-1, 0, 0)
			 */
			switch(prev_sideHit) {
			case 0:
				vectors.add(new Position(0, 1, 0));
			case 1:
				vectors.add(new Position(0, -1, 0));
				break;
			}
			break;
		case all:
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			break;
		}
	}

	public Position getDirection(int sideHit) {
		/*
		 * 2 = (0, 0, 1)
		 * 3 = (0, 0, -1)
		 * 4 = (1, 0, 0)
		 * 5 = (-1, 0, 0)
		 */
		switch(sideHit) {
		case 2:
			return new Position(0, 0, 1);
		case 3:
			return new Position(0, 0, -1);
		case 4:
			return new Position(1, 0, 0);
		case 5:
			return new Position(-1, 0, 0);
		}
		return new Position(0, 0, 0);
	}

	public Set<Position> getBackDirections(int sideHit) {
		Position v = getDirection(sideHit);

		Set<Position> set = new LinkedHashSet();
		if(v.x == 1) {
			set.add(new Position(-1, 0, 1));set.add(new Position(-1, 0, 0));set.add(new Position(-1, 0, -1));
			set.add(new Position(-1, 1, 1));set.add(new Position(-1, 1, 0));set.add(new Position(-1, 1, -1));
			set.add(new Position(-1, -1, 1));set.add(new Position(-1, -1, 0));set.add(new Position(-1, -1, -1));
		} else if(v.x == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(1, 0, 0));set.add(new Position(1, 0, -1));
			set.add(new Position(1, 1, 1));set.add(new Position(1, 1, 0));set.add(new Position(1, 1, -1));
			set.add(new Position(1, -1, 1));set.add(new Position(1, -1, 0));set.add(new Position(1, -1, -1));
		} else if(v.z == 1) {
			set.add(new Position(1, 0, -1));set.add(new Position(0, 0, -1));set.add(new Position(-1, 0, -1));
			set.add(new Position(1, 1, -1));set.add(new Position(0, 1, -1));set.add(new Position(-1, 1, -1));
			set.add(new Position(1, -1, -1));set.add(new Position(0, -1, -1));set.add(new Position(-1, -1, -1));
		} else if(v.z == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(0, 0, 1));set.add(new Position(-1, 0, 1));
			set.add(new Position(1, 1, 1));set.add(new Position(0, 1, 1));set.add(new Position(-1, 1, 1));
			set.add(new Position(1, -1, 1));set.add(new Position(0, -1, 1));set.add(new Position(-1, -1, 1));
		}
		return set;
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		return "StoneBreaker.TickHandler";
	}

}
