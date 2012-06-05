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
import java.util.logging.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.forge.MessageManager;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.NetworkMod;

public class mod_StoneBreakerForge extends NetworkMod implements IConnectionHandler, IPacketHandler , ICommandListener {

	public static String channel = "sbf";

	@MLProp
	public static String mode_line = "ON";
	@MLProp
	public static String mode_tunnel = "ON";
	@MLProp
	public static String mode_upstair = "ON";
	@MLProp
	public static String mode_downstair = "ON";
	@MLProp
	public static String mode_upper = "ON";
	@MLProp
	public static String mode_under = "ON";
	@MLProp
	public static String mode_horizontal = "ON";
	@MLProp
	public static String mode_vertical = "ON";
	@MLProp
	public static String mode_all = "ON";

	public static boolean mode[] = new boolean[10];

	@MLProp(info = "separate by ','")
	public static String blockIDs = "14,15,16,21,56,73,74,89";

	public static Set<Integer> targetIDs = new LinkedHashSet();

	@MLProp(info = "maximum number of block break (0 = unlimited)")
	public static int breaklimit = 0;

	public static final int cmd_break = 0;
	public static final int cmd_mode = 1;
	public static final int cmd_target = 2;
	public static final int cmd_limit = 3;
	public static final int cmd_itembreak = 4;

	public static mod_StoneBreakerForge instance = null;
	public static MinecraftServer minecraftServer = null;

	public static ArrayList<String> userList = new ArrayList<String>();

	class BreakResister {
		public EntityPlayerMP player;
		int i;
		int j;
		int k;
		int blockId;
		int metadata;
		int stacksize;
		int itemdamage;
		World worldObj;

		public BreakResister(EntityPlayerMP entityplayermp, int i, int j, int k, int blockId, int metadata) {
			this.player = entityplayermp;
			this.i = i;
			this.j = j;
			this.k = k;
			this.blockId = blockId;
			this.metadata = metadata;
			this.worldObj = entityplayermp.worldObj;
		}
	}

	static List<BreakResister> breakResisters = new ArrayList();

	public mod_StoneBreakerForge() {
		instance = this;
	}

	@Override
	public String getVersion() {
		return "0.0.8";
	}

	public String GetMode() {
		String strMode = "StoneBreak mode = [";
		if(mode[0]) strMode += "OFF";
		if(mode[1]) strMode += " LINE";
		if(mode[2]) strMode += " TUNNEL";
		if(mode[3]) strMode += " UPSTAIR";
		if(mode[4]) strMode += " DOWNSTAIR";
		if(mode[5]) strMode += " UPPER";
		if(mode[6]) strMode += " UNDER";
		if(mode[7]) strMode += " HORIZONTAL";
		if(mode[8]) strMode += " VERTICAL";
		if(mode[9]) strMode += " ALL";
		strMode += "]";
		return strMode;
	}
	public void loadTarget() {

		mode[0] = true;
		mode[1] = mode_line == "ON";
		mode[2] = mode_tunnel == "ON";
		mode[3] = mode_upstair == "ON";
		mode[4] = mode_downstair == "ON";
		mode[5] = mode_upper == "ON";
		mode[6] = mode_under == "ON";
		mode[7] = mode_horizontal == "ON";
		mode[8] = mode_vertical == "ON";
		mode[9] = mode_all == "ON";
		System.out.println(GetMode());

		String str = blockIDs;
		String[] tokens = str.split(",");
		for(String token : tokens) {
			if(token.isEmpty()) {
				continue;
			}
			targetIDs.add(Integer.parseInt(token.trim()));
		}

		String s = "StoneBreaker target = ";
		s += targetIDs;

		System.out.println(s);
	}

	@Override
	public void load() {
		loadTarget();
		MinecraftForge.registerConnectionHandler(instance);
		ModLoader.setInGameHook(instance, true, true);
	}

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] data) {
		if(channel.equals(this.channel) == false) {
			return;
		}

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

		try {
			int packetType = stream.readInt();
			int as_size = stream.readInt();
			ArrayList<String> as = new ArrayList<String>();
			for(int i = 0; i < as_size; i++) {
				as.add(stream.readUTF());
			}
			int ai_size = stream.readInt();
			ArrayList<Integer> ai = new ArrayList<Integer>();
			for(int i = 0; i < ai_size; i++) {
				ai.add(stream.readInt());
			}

			if(packetType == cmd_break) {
				if(as.size() == 0) return;
				EntityPlayerMP player = minecraftServer.configManager.getPlayerEntity(as.get(0));
				if(player == null) return;
				if(ai.size() < 5) return;
				int i = ai.get(0);
				int j = ai.get(1);
				int k = ai.get(2);
				int blockId = ai.get(3);
				int metadata = ai.get(4);
				BreakResister breakResister = new BreakResister(player, i, j, k, blockId, metadata);
				breakBlock(breakResister);
			}
			else if(packetType == cmd_itembreak) {
				EntityPlayerMP player = minecraftServer.configManager.getPlayerEntity(as.get(0));
				if(player == null) return;
				breakItem(player);
			}

			//System.out.println("RECV: " + channel + " " + packetType + " " + as.toString() + " " + ai.toString());
			//EntityPlayerMP player = minecraftServer.configManager.getPlayerEntity(as.get(0));
			//System.out.println(player.username);

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void breakItem(EntityPlayerMP entityplayermp) {
        ItemStack itemstack = entityplayermp.getCurrentEquippedItem();

        if(itemstack != null) {
	        itemstack.onItemDestroyedByUse(entityplayermp);
	        entityplayermp.destroyCurrentEquippedItem();
        }
	}


	public void breakBlock(BreakResister breakResister) {
		//System.out.println("breakBlock");

		int blockId = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
		if(blockId < 0 || blockId > Block.blocksList.length) {
			return;
		}
		if(Block.blocksList[blockId] == null) {
			return;
		}
		if(targetIDs.contains(blockId) == false) {
			return;
		}

		Material material = breakResister.worldObj.getBlockMaterial(breakResister.i, breakResister.j, breakResister.k);
		if(material.isSolid() == false) {
			return;
		}

		//System.out.printf("breakBlock %d, %d, %d\n", breakResister.i, breakResister.j, breakResister.k);

        if (breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k) != 0)
        {
        	// copy from blockHarvessted
            int i = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
            int j = breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k);
            breakResister.worldObj.playAuxSFXAtEntity(breakResister.player, 2001, breakResister.i, breakResister.j, breakResister.k, i + (breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k) << 12));
            boolean flag = breakResister.player.itemInWorldManager.removeBlock(breakResister.i, breakResister.j, breakResister.k);

            if (breakResister.player.itemInWorldManager.isCreative())
            {
                ((EntityPlayerMP)breakResister.player).playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
            }
            else
            {
                ItemStack itemstack = breakResister.player.getCurrentEquippedItem();
                boolean flag1 = Block.blocksList[blockId].canHarvestBlock(breakResister.player, breakResister.metadata);

                if (itemstack != null)
                {
                    itemstack.onDestroyBlock(i, breakResister.i, breakResister.j, breakResister.k, breakResister.player);

                    if (itemstack.stackSize == 0)
                    {
                        itemstack.onItemDestroyedByUse(breakResister.player);
                        breakResister.player.destroyCurrentEquippedItem();
                    }
                }

                if (flag && flag1)
                {
                    Block.blocksList[i].harvestBlock(breakResister.worldObj, breakResister.player, (int)breakResister.player.posX, (int)breakResister.player.posY, (int)breakResister.player.posZ, j);
                }
            }



        	breakResister.player.playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
        }
	}

	@Override
	public void onConnect(NetworkManager network) {
		MessageManager.getInstance().registerChannel(network, this, channel);

	}

	@Override
	public void onLogin(NetworkManager network, Packet1Login login) {
		userList.add(login.username);
	}

	@Override
	public void onDisconnect(NetworkManager network, String message,
			Object[] args) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public boolean onTickInGame(MinecraftServer minecraftServer) {
		if(this.minecraftServer == null) {
			this.minecraftServer = minecraftServer;
		}
		if(userList.isEmpty() == false) {
			System.out.println(userList.toString());
		}
		ArrayList<String> users = new ArrayList<String>();
		for(String user : userList) {
			if(minecraftServer.configManager.getPlayerEntity(user) == null) {
				users.add(user);
				continue;
			}
			sendBreakMode(user);
			sendTargetIds(user);
			sendBreakLimit(user);
		}
		userList.clear();
		userList.addAll(users);
		return true;
	}

	public void sendBreakMode(String playerName) {
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<Integer> ai = new ArrayList<Integer>();
		for(int i = 0; i < mode.length; i++) {
			ai.add( (mode[i]) ? 1 : 0 );
		}
		sendPacket(cmd_mode, playerName, as, ai);
	}

	public void sendTargetIds(String playerName) {
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<Integer> ai = new ArrayList<Integer>();

		as.add(blockIDs);
		sendPacket(cmd_target, playerName, as, ai);
	}

	public void sendBreakLimit(String playerName) {
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<Integer> ai = new ArrayList<Integer>();
		ai.add(breaklimit);
		sendPacket(cmd_limit, playerName, as, ai);
	}

	public static void sendPacket(int packetType, String playerName, ArrayList<String> as, ArrayList<Integer> ai) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeInt(packetType);
			stream.writeInt(as.size());
			for(String s : as) {
				stream.writeUTF(s);

			}
			stream.writeInt(ai.size());
			for(int i : ai) {
				stream.writeInt(i);
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = channel;
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;
		ModLoader.getMinecraftServerInstance().configManager.sendPacketToPlayer(playerName, packet);

		//String s = "SEND: " + channel + " " + packetType + " " + as.toString() + " " + ai.toString();
		//System.out.println(s);
	}

	@Override
	public boolean onServerCommand(String command, String sender,
			ICommandListener listener) {
		//System.out.println(command + ", " + sender);
		//if(sender != "CONSOLE") return false;
		if(command.startsWith("StoneBreaker") == false) return false;
		String[] op = command.split(" ");

		if(op.length <= 1)  return false;

		Logger minecraftLogger = Logger.getLogger("Minecraft");
		boolean bUpdate = false;
		if(op[1].equalsIgnoreCase("target")) {
			if(op.length > 3) {
				int id = 0;
				boolean bAdd = op[2].equalsIgnoreCase("add");
				boolean bDel = op[2].equalsIgnoreCase("del");
				try {
					id = Integer.parseInt(op[3]);
					bUpdate = true;
				}
				catch(NumberFormatException e) {
					for(Block block : Block.blocksList) {
						if(block == null) continue;
						String blockName = block.getBlockName();
						if(blockName == null) continue;
						blockName = blockName.replaceFirst("tile.", "").replaceFirst("item.", "");
						if(blockName.equalsIgnoreCase(op[3])) {
							id = block.blockID;
							bUpdate = true;
						}
					}
				}

				if(bUpdate) {
					if(bAdd) {
						targetIDs.add(id);
						blockIDs = targetIDs.toString().replace("[", "").replace("]", "");
						String s = "";
						for(int i : targetIDs) {
							Block b = Block.blocksList[i];
							if(b != null) {
								s += " " + b.getBlockName().replace("tile.", "") + "[" + i + "]";
							}
						}

						minecraftLogger.info("StoneBreaker: UPDATED! target = " + s);
					}
					else if(bDel) {
						targetIDs.remove(id);
						minecraftLogger.info("StoneBreaker: UPDATED! target = " + targetIDs.toString());
					}
					else {
						bUpdate = false;
					}
				}
			}
		}
		else if(op[1].equalsIgnoreCase("mode")) {
			if(op.length > 3) {
				bUpdate = true;
				String modeName = op[2];
				boolean flg = false;
				if(op[3].equalsIgnoreCase("ON")) {
					flg = true;
				}
				else if(op[3].equalsIgnoreCase("OFF") == false) {
					bUpdate = false;
				}

				if(bUpdate) {
					if(modeName.equalsIgnoreCase("mode_line")) {
						mode[1] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_tunnel")) {
						mode[2] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_upstair")) {
						mode[3] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_downstair")) {
						mode[4] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_upper")) {
						mode[5] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_under")) {
						mode[6] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_horizontal")) {
						mode[7] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_vertical")) {
						mode[8] = flg;
					}
					else if(modeName.equalsIgnoreCase("mode_all")) {
						mode[9] = flg;
					}
					else {
						bUpdate = false;
					}
				}

				if(bUpdate) {
					minecraftLogger.info("StoneBreaker: UPDATED! mode = " + GetMode());
				}
			}
		}
		else if(op[1].equalsIgnoreCase("limit")) {
			if(op.length > 2) {
				try {
					int i = Integer.parseInt(op[2]);
					breaklimit = i;
					bUpdate = true;
					if(bUpdate) {
						minecraftLogger.info("StoneBreaker: UPDATED! breaklimit = " + breaklimit);
					}
				}
				catch(NumberFormatException e) {
				}
			}
		}

		if(bUpdate) {
			for(String s : minecraftServer.configManager.getPlayerNamesAsList()) {
				userList.add(s);
			}
		}
		else {
			minecraftLogger.info("StoneBreaker: Unknown console command.");
		}
		return true;
	}

	@Override
	public void log(String var1) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public String getUsername() {
		// TODO 自動生成されたメソッド・スタブ
		return "mod_StoneBreaker";
	}

}
