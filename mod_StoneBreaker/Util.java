package mod_StoneBreaker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.PlayerControllerMP;
import net.minecraft.src.ServerConfigurationManager;
import net.minecraft.src.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import cpw.mods.fml.server.FMLServerHandler;

public class Util {
	private static boolean bObfuscate = false;

	public static boolean debug = true;

	public static Object[] getServerWorldAndPlayer(String var0) {
		MinecraftServer server = FMLCommonHandler.instance()
				.getMinecraftServerInstance();
		ServerConfigurationManager config = server
				.getServerConfigurationManager(server);
		EntityPlayerMP player = config.getPlayerForUsername(var0);
		World world = player.worldObj;
		return new Object[] { world, player };
	}

	public static void debugPrintChatMessage(String str) {
		if (debug) {
			ModLoader.getMinecraftInstance().ingameGUI.getChatGUI()
					.printChatMessage(str);
		}
	}

	public static void sendPacketToPlayer(Player player, int type, String[] stringData, int[] integerData) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		stream.writeInt(type);
		stream.writeInt(stringData.length);
		for(String s : stringData) {
			stream.writeUTF(s);

		}
		stream.writeInt(integerData.length);
		for(int i : integerData) {
			stream.writeInt(i);
		}

	    Packet250CustomPayload packet = new Packet250CustomPayload();
	    packet.channel = Config.channel;
	    packet.data = bytes.toByteArray();
	    packet.length = packet.data.length;
		PacketDispatcher.sendPacketToPlayer(packet, player);
	}

	public static void consoleLog(String str) {
		MinecraftServer.logger.info(str);
	}

	public int getBlockHitWait() {
		int blockHitWait = 0;
		Minecraft mc = ModLoader.getMinecraftInstance();

		if(bObfuscate = false) {
			String s = "";
			try {
				if(mc.playerController instanceof PlayerControllerMP) {
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, (PlayerControllerMP)mc.playerController, "blockHitDelay");
				}
				return blockHitWait;
			} catch (IllegalArgumentException e) {
			} catch (SecurityException e) {
			} catch(UnableToAccessFieldException e) {
			}
		}

		bObfuscate  = true;

		String s = "";
		try {
			if(mc.playerController instanceof PlayerControllerMP) {
				blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, (PlayerControllerMP)mc.playerController, 8);
			}
			return blockHitWait;
		} catch (IllegalArgumentException e) {
			s = "not bObfuscate IllegalArgumentException";
			e.printStackTrace();
		} catch (SecurityException e) {
			s = "not bObfuscate SecurityException";
			e.printStackTrace();
		}
		mc.ingameGUI.getChatGUI().printChatMessage(s);

		return -1;
	}

}
