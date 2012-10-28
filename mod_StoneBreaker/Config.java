package mod_StoneBreaker;


import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.src.Block;
import net.minecraft.src.Item;

public class Config {
	public static String channel = "sbf";

	public enum Mode { off, line, tunnel, upstair, downstair, upper, under, horizontal, vertical, all};

	public Set<String> target = new LinkedHashSet();
	public Set<String> tool = new LinkedHashSet();
	public Mode mode = Mode.off;
	public boolean drop_here = true;
	public boolean add_target_permission = true;
	public int limit = 0;

	public Set<Class> getTools() {
		Set<Class> ret = new LinkedHashSet();
		for(String s : tool) {
			try {
				int i = Integer.parseInt(s);
				if(Item.itemsList[i] != null) {
					ret.add(Item.itemsList[i].getClass());
				}
				continue;
			} catch(NumberFormatException e) {
			}

			try {
				ret.add(Class.forName(s));
			} catch(ClassNotFoundException e) {
			}
		}
		return ret;
	}

	public Set<Integer> getTarget() {
		Set<Integer> ret = new LinkedHashSet();

		for(String s : target) {
			try {
				int i = Integer.parseInt(s);
				if(Block.blocksList[i] != null) {
					ret.add(i);
				}
				continue;
			} catch(NumberFormatException e) {
			}

			try {
				Class c = Class.forName(s);
				if(c == null) {
					continue;
				}
				for(Block b : Block.blocksList) {
					if(b == null) {
						continue;
					}
					if(b.getClass() == c) {
						ret.add(b.blockID);
					}
				}
			} catch(ClassNotFoundException e) {
			}
		}
		return ret;
	}

	public Set<String> getTargetNames() {
		Set<String> ret = new LinkedHashSet();

		for(String s : target) {
			try {
				int i = Integer.parseInt(s);
				if(Block.blocksList[i] != null) {
					String name = Block.blocksList[i].getBlockName();
					if(name.startsWith("tile.")) {
						ret.add(name.replace("tile.", ""));
					}
				}
				continue;
			} catch(NumberFormatException e) {
			}

			try {
				Class c = Class.forName(s);
				if(c == null) {
					continue;
				}
				for(Block b : Block.blocksList) {
					if(b == null) {
						continue;
					}
					if(b.getClass() == c) {
						String name = Block.blocksList[b.blockID].getBlockName();
						if(name.startsWith("tile.")) {
							ret.add(name.replace("tile.", ""));
						}
					}
				}
			} catch(ClassNotFoundException e) {
			}
		}
		return ret;
	}

	public void removeTarget(int blockId) {

		for(String s : target) {
			try {
				int i = Integer.parseInt(s);
				if(i == blockId) {
					target.remove(s);
					break;
				}
			} catch(NumberFormatException e) {
			}

			try {
				Class c = Class.forName(s);
				if(c == null) {
					continue;
				}
				for(Block b : Block.blocksList) {
					if(b == null) {
						continue;
					}
					if(b.getClass() == c) {
						if(b.blockID == blockId) {
							target.remove(s);
							break;
						}
					}
				}
			} catch(ClassNotFoundException e) {
			}
		}
	}

	public void ToggleMode() {
		mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
	}

	void setTarget(String additionalWoods) {
		target.clear();
		target.add(Block.wood.getClass().getName());
		for (String token : additionalWoods.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			target.add(token.trim());
		}
	}

	void setTool(String additionalTools) {
		tool.clear();
		for (String token : additionalTools.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			tool.add(token.trim());
		}
	}
}
