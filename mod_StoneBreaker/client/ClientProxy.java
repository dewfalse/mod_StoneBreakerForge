package mod_StoneBreaker.client;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;
import mod_StoneBreaker.CommonProxy;
import mod_StoneBreaker.client.ClientKeyHandler;
import mod_StoneBreaker.client.ClientTickHandler;

public class ClientProxy extends CommonProxy {

	@Override
	public void addKeyBinding() {
		KeyBindingRegistry.registerKeyBinding(new ClientKeyHandler());
	}

	@Override
	public void registerTickHandler() {
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}

}
