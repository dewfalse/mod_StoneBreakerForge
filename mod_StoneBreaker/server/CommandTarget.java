package mod_StoneBreaker.server;

import java.io.IOException;
import java.util.List;

import mod_StoneBreaker.StoneBreaker;
import mod_StoneBreaker.EnumPacketType;
import mod_StoneBreaker.Util;
import net.minecraft.src.Block;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;

public class CommandTarget extends CommandBase {

	@Override
	public String getCommandName() {
		return "StoneBreaker";
	}

	@Override
	public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
		if (par2ArrayOfStr.length > 2) {

			if(par2ArrayOfStr[0].equals("target") == false) {
				return;
			}

			String s = par2ArrayOfStr[2];

			if (par2ArrayOfStr[1].equals("remove")) {
				try {
					int i = Integer.parseInt(s);
					StoneBreaker.config.removeTarget(i);
					notifyAdmins(par1ICommandSender, "commands.StoneBreaker.Target.Removed " + s,
							new Object[] { s });
					notifyConfigToAllPlayers();
					return;
				} catch(NumberFormatException e) {
				}

				try {
					Class c = Class.forName(s);
					if(c == null) {
						return;
					}
					for(Block b : Block.blocksList) {
						if(b == null) {
							continue;
						}
						if(b.getClass() == c) {
							StoneBreaker.config.removeTarget(b.blockID);
							notifyAdmins(par1ICommandSender, "commands.StoneBreaker.Target.Removed " + s,
									new Object[] { s });
							notifyConfigToAllPlayers();
							break;
						}
					}
				} catch(ClassNotFoundException e) {
				}
				return;
			}

			if (par2ArrayOfStr[1].equals("add")) {
				StoneBreaker.config.target.add(s);
				notifyAdmins(par1ICommandSender, "commands.StoneBreaker.Target.Added " + s,
						new Object[] { s });
				notifyConfigToAllPlayers();
				return;
			}
		}

		throw new WrongUsageException("command.StoneBreaker.Target", new Object[0]);
	}

	private void notifyConfigToAllPlayers() {
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
			Util.sendPacketToAllPlayers(EnumPacketType.config.ordinal(), new String[]{target, tool}, new int[]{StoneBreaker.config.limit, StoneBreaker.config.add_target_permission ? 1 : 0});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        return par2ArrayOfStr.length == 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, new String[] {"remove", "add"}): null;
    }
}
