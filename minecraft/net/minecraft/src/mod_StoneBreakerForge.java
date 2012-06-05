package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.src.forge.IChatHandler;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.forge.MessageManager;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.NetworkMod;

public class mod_StoneBreakerForge extends NetworkMod implements IConnectionHandler, IPacketHandler, IChatHandler {

	public static String channel = "sbf";
	class Position {
		int x;
		int y;
		int z;

		public Position(long l, long m, long n) {
			this.x = (int) l;
			this.y = (int) m;
			this.z = (int) n;
		}

		public Position subtract(Position position) {
			return new Position(position.x - x, position.y - y, position.z - z);
		}

		public Position addVector(int x2, int y2, int z2) {
			return new Position(x2 + x, y2 + y, z2 + z);
		}

	    public String toString()
	    {
	        return (new StringBuilder()).append("(").append(x).append(", ").append(y).append(", ").append(z).append(")").toString();
	    }

	    @Override
	    public int hashCode() {
	    	return 13 * 13 * x + 13 * y + z;
	    }

	    @Override
	    public boolean equals(Object obj) {
	    	if(obj == null) return false;
	    	if(obj instanceof Position) {
	    		Position pos = (Position)obj;
	    		if(x == pos.x && y == pos.y && z == pos.z) {
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	}

	class Config {
		public EnumMode mode = EnumMode.off;
		public Set<Integer> targetIDs = new LinkedHashSet();
		public int breaklimit = 0;
		public int distancelimit = 0;
		public boolean virtical_distancelimit = true;
		public boolean droptoplayer = true;
		public boolean[] allow_mode = new boolean[EnumMode.all.ordinal() + 1];
		public Set<String> additional_tools_set = new LinkedHashSet();

		public Config() {
		}

		// load local settings
		public void load() {
			mode = mode_type;

			String str = blockIDs;
			String[] tokens = str.split(",");
			for(String token : tokens) {
				targetIDs.add(Integer.parseInt(token.trim()));
			}
			str = additional_tools;
			tokens = str.split(",");
			for(String token : tokens) {
				additional_tools_set.add(token.trim());
			}

			breaklimit = mod_StoneBreakerForge.breaklimit;
			distancelimit = mod_StoneBreakerForge.distancelimit;
			virtical_distancelimit = mod_StoneBreakerForge.virtical_distancelimit;
			droptoplayer = mod_StoneBreakerForge.droptoplayer;

			for(int i = 0; i < allow_mode.length; i++) {
				allow_mode[i] = true;
			}
		}
	}

	public static Config configLocal = null;
	public static Config configRemote = null;
	public static Config config = configLocal;

	public enum EnumMode {
		off, line, tunnel, upstair, downstair, upper, under, horizontal, vertical, all
	}

	public static KeyBinding key_stonebreak = null;
	public static int prev_i;
	public static int prev_j;
	public static int prev_k;
	public static int sideHit;
	public static int blockId;
	public static int metadata;
	public static int prev_blockHitWait;
	public static Set<Position> vectors = new LinkedHashSet();
	public static Set<Position> positions = new LinkedHashSet();

	@MLProp
	public static String mode = EnumMode.off.toString();
	public static EnumMode mode_type = EnumMode.off;

	@MLProp
	public static boolean effective_tool_only = true;

	@MLProp(info = "separate by ','")
	public static String blockIDs = "14,15,16,21,56,73,74,89";
	public static Set<Integer> targetIDs = new LinkedHashSet();


	@MLProp(info = "additional tools class name(separated by ',')")
	public static String additional_tools = "";

	@MLProp(info = "toggle mode key(default:50 = 'M')")
	public static int mode_key = 50;

	@MLProp(info = "add/remove target key(default:19 = 'R')")
	public static int register_key = 19;

	public static int last_key = -1;
	public static int key_push_times = 0;


	@MLProp(info = "maximum number of block break (0 = unlimited)")
	public static int breaklimit = 0;
	public static int breakcount = 0;

	@MLProp(info = "maximum distance of break from player (0 = unlimited)")
	public static int distancelimit = 0;

	@MLProp(info = "virtical distance limit")
	public static boolean virtical_distancelimit = true;

	@MLProp(info = "drop blocks near by player")
	public static boolean droptoplayer = true;

	public static boolean debugmode = false;
	public static boolean bInit = false;
	public static boolean bObfuscated = true;

	public static final int cmd_break = 0;
	public static final int cmd_mode = 1;
	public static final int cmd_target = 2;
	public static final int cmd_limit = 3;
	public static final int cmd_itembreak = 4;

	public static mod_StoneBreakerForge instance = null;
	public static Minecraft minecraft = null;

	public mod_StoneBreakerForge() {
		instance = this;
	}

	@Override
	public String getVersion() {
		return "0.0.8";
	}

	@Override
	public void load() {
		configLocal = new Config();
		configRemote = new Config();
		configLocal.load();
		MinecraftForge.registerConnectionHandler(instance);
		ModLoader.setInGameHook(instance, true, true);
	}

	@Override
	public void onConnect(NetworkManager network) {
		MessageManager.getInstance().registerChannel(network, this, channel);

	}

	@Override
	public void onLogin(NetworkManager network, Packet1Login login) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void onDisconnect(NetworkManager network, String message,
			Object[] args) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] data) {
		if(channel.equals(this.channel) == false) {
			return;
		}

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

		int packetType = -1;
		try {
			packetType = stream.readInt();
			int as_size = stream.readInt();
			String[] as = new String[as_size];
			for(int i = 0; i < as_size; i++) {
				as[i] = stream.readUTF();
			}
			int ai_size = stream.readInt();
			int[] ai = new int[ai_size];
			for(int i = 0; i < ai_size; i++) {
				ai[i] = stream.readInt();
			}

			switch(packetType) {
			case cmd_mode:
				for(int i = 0; i < configRemote.allow_mode.length; i++) {
					configRemote.allow_mode[i] = (ai[i] == 1);
				}
				bInit = false;
				break;
			case cmd_limit:
				configRemote.breaklimit = ai[0];
				bInit = false;
				break;
			case cmd_target:
				configRemote.targetIDs.clear();
				String str = as[0];
				String[] tokens = str.split(",");
				try {
					for(String token : tokens) {
						if(token.isEmpty()) {
							continue;
						}
						configRemote.targetIDs.add(Integer.parseInt(token.trim()));
					}
				} catch(NumberFormatException e) {

				}
				bInit = false;
				break;
			}

		    if(minecraft != null) {
		    	String s = channel + " " + packetType + " " + as.toString() + " " + ai.toString();

		    	//minecraft.ingameGUI.addChatMessage(s);
		    }
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTickInGame(float var1, Minecraft var2) {
		if(minecraft == null) {
			minecraft = var2;
		}

		if(minecraft.theWorld.isRemote) {
			config = configRemote;
		}
		else {
			config = configLocal;
		}

		if(setMode()) {
			printMode(minecraft);
		}

		if(minecraft.theWorld.isRemote == false) {
			if(Keyboard.isKeyDown(register_key)) {
				key_push_times++;
				last_key = register_key;
			} else if(key_push_times > 0 && last_key == register_key) {
				Block b = Block.blocksList[blockId];
				String s = "StoneBreaker ";
				if(config.targetIDs.contains(blockId) == false) {
					config.targetIDs.add(blockId);
					s = "add target : ";
				} else {
					config.targetIDs.remove(blockId);
					s = "remove target : ";
				}
				if(b != null) {
					s += b.getBlockName().replace("tile.", "");
				}
				s += "["+ blockId + "]";
				minecraft.ingameGUI.addChatMessage(s);
				printTargetIDs(minecraft);
				key_push_times = 0;
			}
		}

		if(minecraft.theWorld.isRemote) {
			if(bInit == false) {
				if(config.targetIDs.isEmpty() == false) {
					printAllowMode(minecraft);
					printLimit(minecraft);
					printTargetIDs(minecraft);
					bInit = true;
				}
			}
		}
		else {
			if(configRemote.targetIDs.isEmpty() == false) {
				if(bInit == false) {
					printMode(minecraft);
					printTargetIDs(minecraft);
					bInit = true;
				}
			}
		}


		boolean breakflag = false;

		String varName = getBlockHitWaitName(minecraft);
		Class c = getPlayerControllerClass(minecraft);

		try {
			int blockHitWait = (Integer) ModLoader.getPrivateValue(c, minecraft.playerController, varName);
			if(blockHitWait == 5 && blockHitWait != prev_blockHitWait) {
				breakflag = true;
			}
			prev_blockHitWait = blockHitWait;
		} catch (IllegalArgumentException e) {
			bObfuscated = false;
			e.printStackTrace();
		} catch (SecurityException e) {
			bObfuscated = false;
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			bObfuscated = false;
			e.printStackTrace();
		}

		if(breakflag) {
			if(config.targetIDs.contains(blockId)) {
				startBreak(minecraft);
			} else {
				if(debugmode) {
					System.out.print("BlockId ");
					System.out.print(blockId);
					System.out.print(" not in targetIDs ");
					System.out.println(config.targetIDs);
				}
			}
		}

		if(positions.size() > 0) {
			continueBreak(minecraft);
			return true;
		}

		if (minecraft.objectMouseOver == null) {
			return true;
		}

		if (minecraft.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			prev_i = minecraft.objectMouseOver.blockX;
			prev_j = minecraft.objectMouseOver.blockY;
			prev_k = minecraft.objectMouseOver.blockZ;
			sideHit = minecraft.objectMouseOver.sideHit;
			blockId = minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k);
			Block block = Block.blocksList[blockId];
			metadata = minecraft.theWorld.getBlockMetadata(prev_i, prev_j, prev_k);
			Material material = minecraft.theWorld.getBlockMaterial(prev_i, prev_j, prev_k);
			if(breakflag) {
				if(debugmode) System.out.println(getDirection(minecraft));
			}
		}

		return true;
	}

	public static void sendPacket(int packetType, String[] as, int[] ai) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeInt(packetType);
			stream.writeInt(as.length);
			for(String s : as) {
				stream.writeUTF(s);

			}
			stream.writeInt(ai.length);
			for(int i : ai) {
				stream.writeInt(i);
			}
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
	    Packet250CustomPayload packet = new Packet250CustomPayload();
	    packet.channel = channel;
	    packet.data = bytes.toByteArray();
	    packet.length = packet.data.length;
	    ModLoader.sendPacket(packet);

	    if(minecraft != null) {
	    	String s = packet.channel + " " + packetType + " " + packet.length;

	    	//minecraft.ingameGUI.addChatMessage(s);
	    }
	}

	public void printTargetIDs(Minecraft minecraft) {
		String s = "StoneBreaker target :";
		for(int id : config.targetIDs) {
			Block b = Block.blocksList[id];
			if(b != null) {
				s += " " + b.getBlockName().replace("tile.", "") + "[" + id + "]";
			}
		}
		//s += targetIDs;
		minecraft.ingameGUI.addChatMessage(s);
	}
	public void printMode(Minecraft minecraft) {
		String s = "StoneBreaker mode = ";
		s += config.mode.toString();
		minecraft.ingameGUI.addChatMessage(s);
	}
	public void printAllowMode(Minecraft minecraft) {
		String s = "StoneBreaker mode :";
		for(EnumMode e : EnumMode.values()) {
			if(config.allow_mode[e.ordinal()]) {
				s += " " + e.toString();
			}
		}
		minecraft.ingameGUI.addChatMessage(s);
	}
	public void printLimit(Minecraft minecraft) {
		String s = "StoneBreaker breaklimit : ";
		s += config.breaklimit;
		minecraft.ingameGUI.addChatMessage(s);
	}

	// blockHitWait obfuscate i
	String getBlockHitWaitName(Minecraft minecraft) {
		if(minecraft.playerController instanceof PlayerControllerMP) {
			if(bObfuscated) {
				return "i";
			}
			return "blockHitDelay";
		}
		else if(minecraft.playerController instanceof PlayerControllerSP) {
			if(bObfuscated) {
				return "i";
			}
			return "blockHitWait";
		}
		else {
			if(bObfuscated) {
				return "c";
			}
			return "field_35647_c";
		}
	}

	Class getPlayerControllerClass(Minecraft minecraft) {
		if(minecraft.playerController instanceof PlayerControllerMP) {
			return PlayerControllerMP.class;
		}
		else if(minecraft.playerController instanceof PlayerControllerSP) {
			return PlayerControllerSP.class;
		}
		return PlayerControllerCreative.class;
	}

	public boolean setMode() {
		if(Keyboard.isKeyDown(mode_key)) {
			key_push_times++;
			last_key = mode_key;
		}
		else if(key_push_times > 0 && last_key == mode_key) {
			int i = 1;
			while(true) {
				int index = (config.mode.ordinal() + i) % EnumMode.values().length;
				if(config.allow_mode[index]) {
					config.mode = EnumMode.values()[index];
					key_push_times = 0;
					break;
				}
				i++;
			}
			return true;
		}
		return false;
	}

	private void continueBreak(Minecraft minecraft) {
		if(debugmode) {
			System.out.print("continueBreak start ");
			System.out.println(positions);
		}
		int i = 0;
		Set<Position> oldPositions = new LinkedHashSet();
		Set<Position> newPositions = new LinkedHashSet();
		oldPositions.addAll(positions);

		// ���Ӄu���b�N������ɗ\��
		for(Position position : oldPositions) {
			for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
				if(positions.contains(new_pos) == false) {
					newPositions.add(new_pos);
				}
			}
		}
		// �\��ς݃u���b�N��j��
		for(Position position : oldPositions) {
			// �c�[���j���͏I��
			if(breakBlock(minecraft, position) == false) {
				positions.clear();
				return;
			}
			// �ꊇ�j�����͏I��
			if(config.breaklimit > 0 && breakcount >= config.breaklimit) {
				positions.clear();
				breakcount = 0;
				return;
			}
		}

		positions = newPositions;
		if(positions.size() > 1) {
			if(debugmode) {
				System.out.print("continueBreak end");
				System.out.println(positions);
			}

		}
		if(debugmode) {
			System.out.print("continueBreak end");
			System.out.println(positions);
		}
	}

	// true : continue break
	// false: stop break
	private boolean breakBlock(Minecraft minecraft, Position position) {
		if(debugmode) {
			System.out.print("breakBlock ");
			System.out.print(position.toString());
			System.out.println(" start");
		}

		if(position.y < 1 || position.y > 255) {
			return true;
		}


        ItemStack itemstack = minecraft.thePlayer.getCurrentEquippedItem();
        if (itemstack == null)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack == null)");
        	return false;
        }
        if (itemstack.stackSize == 0)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack.stackSize == 0)");
        	return false;
        }
		int id = minecraft.theWorld.getBlockId((int)position.x, (int)position.y, (int)position.z);
		boolean bSame = false;
		if(id == blockId) {
			bSame = true;
		}
		if(id == Block.dirt.blockID && blockId == Block.grass.blockID) {
			bSame = true;
		}
		if(blockId == Block.dirt.blockID && id == Block.grass.blockID) {
			bSame = true;
		}
		if(id == Block.oreRedstone.blockID && blockId == Block.oreRedstoneGlowing.blockID) {
			bSame = true;
		}
		if(blockId == Block.oreRedstoneGlowing.blockID && id == Block.oreRedstone.blockID) {
			bSame = true;
		}
		if(bSame == false) {
			if(debugmode) System.out.println("breakBlock skip(BlockId)");
			return true;
		}

		// ignore liquid and air
		Block block = Block.blocksList[id];
		if(block == null) {
			return false;
		}
		if(block.blockMaterial.isLiquid() || block.blockMaterial == Material.air) {
			if(debugmode) System.out.println("breakBlock skip(blockMaterial)");
			return true;
		}

		// ignore bedrock
		if(blockId == Block.bedrock.blockID) {
			return true;
		}

        int i = minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z);

		// item require pickaxe or axe or shovel or shears
		Item item = Item.itemsList[itemstack.itemID];
    	String itemName = Item.itemsList[itemstack.itemID].getClass().getName();
    	//minecraft.ingameGUI.addChatMessage(itemName);
    	//minecraft.ingameGUI.addChatMessage(config.additional_tools_set.toString());
    	if(item instanceof ItemTool || config.additional_tools_set.contains(itemName)) {
			if(effective_tool_only && item.getStrVsBlock(itemstack, block, i) <= 1.0F) {
				return false;
			}

		} else if(itemstack.itemID == Item.shears.shiftedIndex) {

		} else {
			if(debugmode) System.out.println("breakBlock skip(Item not ItemTool)");
			return false;
		}
		//if(itemstack.getStrVsBlock(block) <= 1.0F) {
		//	System.out.println("breakBlock skip(Str)");
		//	return false;
		//}

        boolean ret = true;
        if(minecraft.theWorld.isRemote) {
    		EntityClientPlayerMP player = ((EntityClientPlayerMP)minecraft.thePlayer);

            int currentItem = minecraft.thePlayer.inventory.currentItem;
            player.sendQueue.addToSendQueue(new Packet16BlockItemSwitch(currentItem));

            {
                String[] as = {minecraft.thePlayer.username};
                int[] ai = {(int)position.x, (int)position.y, (int)position.z, id, i};
                sendPacket(cmd_break, as, ai);
            }
    		//if(debugmode) System.out.printf("[%d] send %d, %d, %d, %d, %d, %d\n", packet.channel, packet.dataInt[0], packet.dataInt[1], packet.dataInt[2], packet.dataInt[3], packet.dataInt[4], packet.dataInt[5]);

        	block.onBlockDestroyedByPlayer(minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
    		player.sendQueue.addToSendQueue(new Packet14BlockDig(2, (int)position.x, (int)position.y, (int)position.z, 0));
            minecraft.theWorld.playAuxSFX(2001, (int)position.x, (int)position.y, (int)position.z, block.blockID + (minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z) << 12));
            boolean flag = minecraft.theWorld.setBlockWithNotify((int)position.x, (int)position.y, (int)position.z, 0);

            if (block != null && flag)
            {
                block.onBlockDestroyedByPlayer( minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
            }

            itemstack.onDestroyBlock(i, (int)position.x, (int)position.y, (int)position.z, minecraft.thePlayer);

            if (itemstack.stackSize == 0)
            {
                itemstack.onItemDestroyedByUse(minecraft.thePlayer);
                minecraft.thePlayer.destroyCurrentEquippedItem();

                String[] as = {minecraft.thePlayer.username};
                int[] ai = {};
                sendPacket(cmd_itembreak, as, ai);

                ret = false;
            }
        }
        else {
            int currentItem = minecraft.thePlayer.inventory.currentItem;

            minecraft.theWorld.playAuxSFX(2001, (int)position.x, (int)position.y, (int)position.z, block.blockID + (minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z) << 12));
            boolean flag = minecraft.theWorld.setBlockWithNotify((int)position.x, (int)position.y, (int)position.z, 0);

            if (block != null && flag)
            {
                block.onBlockDestroyedByPlayer( minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
            }

            itemstack.onDestroyBlock(i, (int)position.x, (int)position.y, (int)position.z, minecraft.thePlayer);
            if (itemstack.stackSize == 0)
            {
                itemstack.onItemDestroyedByUse(minecraft.thePlayer);
                minecraft.thePlayer.destroyCurrentEquippedItem();
                ret = false;
            }

            boolean flag1 = minecraft.thePlayer.canHarvestBlock(block);

            if (flag1)
            {
            	block.harvestBlock(minecraft.theWorld, minecraft.thePlayer, (int)minecraft.thePlayer.posX, (int)minecraft.thePlayer.posY, (int)minecraft.thePlayer.posZ, i);
            }
        }

		breakcount++;

		if(debugmode) System.out.println("breakBlock end");
		if(config.breaklimit > 0 && breakcount > config.breaklimit) {
			breakcount = 0;
			return false;
		}
		return ret;
	}

	public Position getDirection(Minecraft minecraft) {
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

	public Set<Position> getBackDirections(Minecraft minecraft) {
		Position v = getDirection(minecraft);

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

	public void startBreak(Minecraft minecraft) {
		if(debugmode) System.out.println("startBreak start");

		Set<Position> backDirections = getBackDirections(minecraft);
		positions.clear();
		vectors.clear();
		Position v;

		switch(config.mode) {
		case off:
			break;
		case line:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(getDirection(minecraft));
			break;
		case tunnel:
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			positions.add(new Position(prev_i, prev_j + 2, prev_k));
			vectors.add(getDirection(minecraft));
			break;
		case downstair:
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			positions.add(new Position(prev_i, prev_j + 2, prev_k));
			switch(sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				v = getDirection(minecraft);
				v.y = -1;
				vectors.add(v);
				break;
			}
			break;
		case upstair:
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			positions.add(new Position(prev_i, prev_j + 2, prev_k));
			switch(sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				v = getDirection(minecraft);
				v.y = 1;
				vectors.add(v);
				break;
			}
			break;
		case upper:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			switch(sideHit) {
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
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			switch(sideHit) {
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
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 0, 0));vectors.add(new Position(-1, 0, 0));
			vectors.add(new Position(0, 0, 1));vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, -1));
			break;
		case vertical:
			positions.add(new Position(prev_i, prev_j, prev_k));

			/*
			 * 2 = (0, 0, 1)
			 * 3 = (0, 0, -1)
			 * 4 = (1, 0, 0)
			 * 5 = (-1, 0, 0)
			 */
			switch(sideHit) {
			case 0:
				vectors.add(new Position(0, 1, 0));
			case 1:
				vectors.add(new Position(0, -1, 0));
				break;
			}
			break;
		case all:
			positions.add(new Position(prev_i, prev_j, prev_k));
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
		if(debugmode) {
			System.out.print("positions ");
			System.out.println(positions);
			System.out.print("vectors ");
			System.out.println(vectors);
			System.out.print(minecraft.theWorld.getBlockId(prev_i+1, prev_j, prev_k));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i-1, prev_j, prev_k));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k+1));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k-1));
			System.out.println();
		}

		if(debugmode) System.out.println("startBreak end");
	}

	public Set<Position> addNextBreakBlocks(Minecraft minecraft, Position position) {
		Set<Position> newPositions = new LinkedHashSet();
		for(Position vector : vectors) {
			if(vector.x == 0 && vector.y == 0 && vector.z == 0) {
				continue;
			}

			Position pos = position.addVector(vector.x, vector.y, vector.z);

			if(config.distancelimit > 0) {
				if(Math.abs((int)Math.round(minecraft.thePlayer.posX) - pos.x) > config.distancelimit) {
					continue;
				}
				if(config.virtical_distancelimit && Math.abs((int)Math.round(minecraft.thePlayer.posY) - pos.y) > config.distancelimit) {
					continue;
				}
				if(Math.abs((int)Math.round(minecraft.thePlayer.posZ) - pos.z) > config.distancelimit) {
					continue;
				}

			}

			int id = minecraft.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
			boolean bSame = false;
			if(id == blockId) {
				bSame = true;
			}
			if(id == Block.dirt.blockID && blockId == Block.grass.blockID) {
				bSame = true;
			}
			if(blockId == Block.dirt.blockID && id == Block.grass.blockID) {
				bSame = true;
			}
			if(id == Block.oreRedstone.blockID && blockId == Block.oreRedstoneGlowing.blockID) {
				bSame = true;
			}
			if(blockId == Block.oreRedstoneGlowing.blockID && id == Block.oreRedstone.blockID) {
				bSame = true;
			}

			if(bSame) {
				if(positions.contains(pos) == false && newPositions.contains(pos) == false) {
					if(debugmode) {
						System.out.print("addNextBreakBlocks ");
						System.out.println(pos.toString());
					}
					newPositions.add(pos);
				}
			}
		}

		return newPositions;
	}

	@Override
	public String onServerChat(EntityPlayer player, String message) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	@Override
	public boolean onChatCommand(EntityPlayer player, boolean isOp,
			String command) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return false;
	}

	@Override
	public boolean onServerCommand(Object listener, String username,
			String command) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return false;
	}

	@Override
	public String onServerCommandSay(Object listener, String username,
			String message) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	@Override
	public String onClientChatRecv(String message) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}
}
