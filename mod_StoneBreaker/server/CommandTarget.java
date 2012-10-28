package mod_StoneBreaker.server;

import java.util.List;

import mod_StoneBreaker.StoneBreaker;
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
				return;
			}
		}

		throw new WrongUsageException("command.StoneBreaker.Target", new Object[0]);
	}

    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        return par2ArrayOfStr.length == 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, new String[] {"remove", "add"}): null;
    }
}
