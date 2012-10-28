package mod_StoneBreaker;


import java.util.logging.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.MLProp;
import net.minecraft.src.ServerCommandManager;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.server.FMLServerHandler;

@Mod(modid = "StoneBreaker", name = "StoneBreaker", version = "1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = true, channels = { "sbf" }, packetHandler = SBFPacketHandler.class, connectionHandler = SBFConnectionHandler.class, versionBounds = "[1.0]")
public class StoneBreaker {
	@SidedProxy(clientSide = "mod_StoneBreaker.client.ClientProxy", serverSide = "mod_StoneBreaker.server.ServerProxy")
	public static CommonProxy proxy;

	@Instance("StoneBreaker")
	public static StoneBreaker instance;

	public static Logger logger = Logger.getLogger("Minecraft");

	public static Config config = new Config();

	@Mod.Init
	public void load(FMLInitializationEvent event) {
		proxy.addKeyBinding();
		proxy.registerTickHandler();
		proxy.registerCommand();
		NetworkRegistry.instance().registerGuiHandler(instance, proxy);

		logger.info("mod_StoneBreaker.load");
	}

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(
				event.getSuggestedConfigurationFile());

		cfg.load();

		Property tools = cfg.get(Configuration.CATEGORY_GENERAL,
				"AdditionalTools", "");
		tools.comment = "additional tools class name, separated by ','";
		String additionalTools = tools.value;

		Property target = cfg.get(Configuration.CATEGORY_GENERAL,
				"TargetBlockID", "");
		target.comment = "Additional block IDs, separate by ','";
		String additionalTargets = target.value;

		Property drop_here = cfg.get(Configuration.CATEGORY_GENERAL,
				"drop_here", true);
		drop_here.comment = "Harvest items near by player";
		config.drop_here = drop_here.getBoolean(true);

		Property limit = cfg.get(Configuration.CATEGORY_GENERAL,
				"limit", 0);
		limit.comment = "Limit number of break at once";
		config.limit = limit.getInt();

		Property add_target = cfg.get(Configuration.CATEGORY_GENERAL,
				"add_target_flg", true);
		add_target.comment = "Add/Remove targets in game(Register Key)";
		config.add_target_permission = add_target.getBoolean(true);

		cfg.save();

		Property debug = cfg.get(Configuration.CATEGORY_GENERAL,
				"debug", true);
		Util.debug = debug.getBoolean(true);


		for (String token : additionalTools.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			config.tool.add(token.trim());
		}

		for (String token : additionalTargets.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			config.target.add(token.trim());
		}
	}
}
