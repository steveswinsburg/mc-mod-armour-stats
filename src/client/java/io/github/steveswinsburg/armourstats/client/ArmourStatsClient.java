package io.github.steveswinsburg.armourstats.client;

import io.github.steveswinsburg.armourstats.ArmourStatsMod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.resources.Identifier;

/**
 * 
 * ArmourStatsClient
 */
public class ArmourStatsClient implements ClientModInitializer {
	public static final Identifier ARMOUR_STATS_LAYER =
			Identifier.fromNamespaceAndPath(ArmourStatsMod.MOD_ID, "armour_stats");

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(ARMOUR_STATS_LAYER, new ArmourStatsHud());
		ArmourStatsCommands.register();
		ArmourStatsMod.LOGGER.info("Armour Stats HUD registered");
	}
}
