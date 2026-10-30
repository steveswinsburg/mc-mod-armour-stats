package io.github.steveswinsburg.armourstats.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.mojang.brigadier.Command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.network.chat.Component;

/**
 * Setup the commands for toggling / setting the armour HUD display mode.
 *
 * <pre>
 * /armourstats              – show the current mode
 * /armourstats toggle       – cycle panel ↔ hotbar
 * /armourstats mode         – cycle panel ↔ hotbar
 * /armourstats mode panel   – detailed top-left panel
 * /armourstats mode hotbar  – icon row left of the hotbar
 * </pre>
 */
public final class ArmourStatsCommands {
	private ArmourStatsCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				literal("armourstats")
						.executes(ctx -> showMode(ctx.getSource()))
						.then(literal("toggle")
								.executes(ctx -> toggleMode(ctx.getSource())))
						.then(literal("mode")
								.executes(ctx -> toggleMode(ctx.getSource()))
								.then(literal("panel")
										.executes(ctx -> setMode(ctx.getSource(), DisplayMode.PANEL)))
								.then(literal("hotbar")
										.executes(ctx -> setMode(ctx.getSource(), DisplayMode.HOTBAR))))));
	}

	// show the current mode
	private static int showMode(FabricClientCommandSource source) {
		source.sendFeedback(Component.literal(
				"Armour Stats display mode: " + ArmourStatsConfig.getDisplayMode().id()
						+ " (use /armourstats toggle to switch)"));
		return Command.SINGLE_SUCCESS;
	}

	// toggle the current mode
	private static int toggleMode(FabricClientCommandSource source) {
		DisplayMode mode = ArmourStatsConfig.toggleDisplayMode();
		source.sendFeedback(Component.literal("Armour Stats display mode set to " + mode.id()));
		return Command.SINGLE_SUCCESS;
	}

	// set the current mode
	private static int setMode(FabricClientCommandSource source, DisplayMode mode) {
		ArmourStatsConfig.setDisplayMode(mode);
		source.sendFeedback(Component.literal("Armour Stats display mode set to " + mode.id()));
		return Command.SINGLE_SUCCESS;
	}
}
