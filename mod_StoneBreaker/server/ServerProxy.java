package mod_StoneBreaker.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ServerCommandManager;
import cpw.mods.fml.server.FMLServerHandler;
import mod_StoneBreaker.CommonProxy;

public class ServerProxy extends CommonProxy {
	@Override
	public void registerCommand() {

		if(FMLServerHandler.instance() == null) {
			return;
		}

		if(FMLServerHandler.instance().getServer() == null) {
			return;
		}

		if(FMLServerHandler.instance().getServer().getCommandManager() instanceof ServerCommandManager) {
			ServerCommandManager manager = (ServerCommandManager)FMLServerHandler.instance().getServer().getCommandManager();
			manager.registerCommand(new CommandTarget());
            MinecraftServer.logger.info("Register StoneBreaker Command(/StoneBreaker target add/remove (blockID)");
		}
	}

}
