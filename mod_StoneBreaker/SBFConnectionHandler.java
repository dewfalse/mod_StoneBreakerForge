package mod_StoneBreaker;

import java.io.IOException;

import mod_StoneBreaker.EnumPacketType;
import mod_StoneBreaker.StoneBreaker;
import mod_StoneBreaker.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetLoginHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class SBFConnectionHandler implements IConnectionHandler {

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler,
			NetworkManager manager) {
		try {
			String target = "";
			for(int i : StoneBreaker.config.getTarget()) {
				target += String.valueOf(i);
				target += ",";
			}
			String tool = "";
			for(Class c : StoneBreaker.config.getTools()) {
				tool += c.toString();
				tool += ",";
			}
			Util.sendPacketToPlayer(player, EnumPacketType.config.ordinal(), new String[]{target, tool}, new int[]{StoneBreaker.config.limit, StoneBreaker.config.add_target_permission ? 1 : 0});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler,
			NetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server,
			int port, NetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void connectionOpened(NetHandler netClientHandler,
			MinecraftServer server, NetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void connectionClosed(NetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler,
			NetworkManager manager, Packet1Login login) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
