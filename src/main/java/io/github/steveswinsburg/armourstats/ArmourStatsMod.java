package io.github.steveswinsburg.armourstats;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialise the mod.
 */
public class ArmourStatsMod implements ModInitializer {
	public static final String MOD_ID = "armourstats";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Armour Stats HUD loaded");
	}
}
