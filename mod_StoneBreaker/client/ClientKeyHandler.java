package mod_StoneBreaker.client;

import java.util.EnumSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import mod_StoneBreaker.StoneBreaker;
import mod_StoneBreaker.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class ClientKeyHandler extends KeyHandler {
	static KeyBinding modeBinding = new KeyBinding("StoneBreaker Mode", Keyboard.KEY_M);
	static KeyBinding registerBinding = new KeyBinding("StoneBreaker Register", Keyboard.KEY_R);

	public ClientKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super(keyBindings, repeatings);
	}

	public ClientKeyHandler() {
		super(new KeyBinding[]{modeBinding, registerBinding}, new boolean[]{false, false});
	}

	@Override
	public String getLabel() {
		return "StoneBreaker.KeyHandler";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if(tickEnd) {
			if(kb == modeBinding) {
				StoneBreaker.config.ToggleMode();
				Util.printChatMessage("StoneBreaker Mode: " + StoneBreaker.config.mode.toString());
			}
			if(kb == registerBinding) {
				if(StoneBreaker.config.add_target_permission == false) {
					return;
				}

				Minecraft mc = ModLoader.getMinecraftInstance();

				if (mc.objectMouseOver == null) {
					return;
				}

				if (mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
					int i = mc.objectMouseOver.blockX;
					int j = mc.objectMouseOver.blockY;
					int k = mc.objectMouseOver.blockZ;
					int blockId = mc.theWorld.getBlockId(i, j, k);
					if(blockId == 0) {
						return;
					}
					if(Block.blocksList[blockId] == null) {
						return;
					}
					String name = Block.blocksList[blockId].getBlockName();
					if(name.startsWith("tile.")) {
						name = name.replace("tile.", "");
					}

					Set<Integer> targets = StoneBreaker.config.getTarget();
					if(targets.contains(blockId)) {
						StoneBreaker.config.removeTarget(blockId);
						Util.printChatMessage("StoneBreaker Target Remove: " + name);
					}
					else {
						StoneBreaker.config.target.add(String.valueOf(blockId));
						Util.printChatMessage("StoneBreaker Target Add: " + name + "(" + String.valueOf(blockId) + ")");
					}
					Util.printChatMessage("StoneBreaker Targets: " + StoneBreaker.config.getTargetNames().toString());
				}
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
