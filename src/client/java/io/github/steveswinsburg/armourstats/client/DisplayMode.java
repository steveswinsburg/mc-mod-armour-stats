package io.github.steveswinsburg.armourstats.client;

/**
 * Enum for the different display modes.
 */
public enum DisplayMode {

	// top left panel
	PANEL("panel"),
	
	//hotbar icon row
	HOTBAR("hotbar");

	private final String id;

	DisplayMode(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	// allow toggling between the two modes
	public DisplayMode next() {
		DisplayMode[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	// map value back to the enum
	public static DisplayMode fromId(String id) {
		for (DisplayMode mode : values()) {
			if (mode.id.equalsIgnoreCase(id)) {
				return mode;
			}
		}
		return PANEL;
	}
}
