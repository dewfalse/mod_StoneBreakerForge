package mod_StoneBreaker;

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
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler,
			NetworkManager manager) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server,
			int port, NetworkManager manager) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void connectionOpened(NetHandler netClientHandler,
			MinecraftServer server, NetworkManager manager) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void connectionClosed(NetworkManager manager) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler,
			NetworkManager manager, Packet1Login login) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

}
