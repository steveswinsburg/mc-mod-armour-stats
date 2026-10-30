package io.github.steveswinsburg.armourstats.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.minecraft.client.Minecraft;

import io.github.steveswinsburg.armourstats.ArmourStatsMod;

/**
 * Client-side settings persisted under {@code config/armourstats.properties}.
 */
public final class ArmourStatsConfig {
	
	private static final String KEY_DISPLAY_MODE = "displayMode";

	//default display mode is hotbar
	private static DisplayMode displayMode = DisplayMode.HOTBAR;
	
	private static boolean loaded;

	private ArmourStatsConfig() {
	}

	public static DisplayMode getDisplayMode() {
		ensureLoaded();
		return displayMode;
	}

	public static void setDisplayMode(DisplayMode mode) {
		ensureLoaded();
		displayMode = mode;
		save();
	}

	public static DisplayMode toggleDisplayMode() {
		DisplayMode next = getDisplayMode().next();
		setDisplayMode(next);
		return next;
	}

	private static void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}

	private static Path configPath() {
		return Minecraft.getInstance().gameDirectory.toPath()
				.resolve("config")
				.resolve("armourstats.properties");
	}

	// load the config
	private static void load() {
		loaded = true;
		Path path = configPath();
		if (!Files.isRegularFile(path)) {
			return;
		}

		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path)) {
			properties.load(reader);
			displayMode = DisplayMode.fromId(properties.getProperty(KEY_DISPLAY_MODE, DisplayMode.PANEL.id()));
		} catch (IOException e) {
			ArmourStatsMod.LOGGER.warn("Failed to load Armour Stats config from {}", path, e);
		}
	}

	// save the config
	private static void save() {
		Path path = configPath();
		Properties properties = new Properties();
		properties.setProperty(KEY_DISPLAY_MODE, displayMode.id());

		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				properties.store(writer, "Armour Stats HUD settings");
			}
		} catch (IOException e) {
			ArmourStatsMod.LOGGER.warn("Failed to save Armour Stats config to {}", path, e);
		}
	}
}
