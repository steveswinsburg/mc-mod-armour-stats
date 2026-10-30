package com.steveswinsburg.armourstats.client;

import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a compact panel in the top-left of the HUD showing every equipped
 * armour piece, its remaining durability and the player's total armour points.
 */
public class ArmourStatsHud implements HudElement {
	private static final List<SlotLabel> SLOTS = List.of(
			new SlotLabel(EquipmentSlot.HEAD, "Head"),
			new SlotLabel(EquipmentSlot.CHEST, "Chest"),
			new SlotLabel(EquipmentSlot.LEGS, "Legs"),
			new SlotLabel(EquipmentSlot.FEET, "Feet"));

	private static final int MARGIN = 4;
	private static final int PADDING = 4;
	private static final int ICON = 16;
	private static final int ROW_HEIGHT = 20;
	private static final int TEXT_GAP = 4;
	private static final int PANEL_WIDTH = 150;

	private static final int COLOUR_BACKGROUND = 0x90000000;
	private static final int COLOUR_TITLE = 0xFFFFD700;
	private static final int COLOUR_LABEL = 0xFFFFFFFF;
	private static final int COLOUR_EMPTY = 0xFF808080;
	private static final int COLOUR_GOOD = 0xFF55FF55;
	private static final int COLOUR_WARN = 0xFFFFFF55;
	private static final int COLOUR_BAD = 0xFFFF5555;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		// HudElementRegistry.addLast does not inherit vanilla HUD visibility, so
		// also honour F1 (Hud.isHidden) so this panel disappears with the rest of the HUD.
		if (player == null || minecraft.gui.hud.isHidden()) {
			return;
		}

		Font font = minecraft.font;

		int rows = SLOTS.size();
		int panelHeight = PADDING + font.lineHeight + PADDING
				+ (rows * ROW_HEIGHT)
				+ font.lineHeight + PADDING;

		int left = MARGIN;
		int top = MARGIN;
		int right = left + PANEL_WIDTH;
		int bottom = top + panelHeight;

		graphics.fill(left, top, right, bottom, COLOUR_BACKGROUND);

		int textX = left + PADDING;
		int y = top + PADDING;

		graphics.text(font, Component.literal("Armour Stats"), textX, y, COLOUR_TITLE);
		y += font.lineHeight + PADDING;

		for (SlotLabel slot : SLOTS) {
			ItemStack stack = player.getItemBySlot(slot.slot());
			int iconX = textX;
			int iconY = y;
			int labelX = iconX + ICON + TEXT_GAP;
			int labelMaxWidth = right - PADDING - labelX;

			if (stack.isEmpty()) {
				graphics.fill(iconX, iconY, iconX + ICON, iconY + ICON, 0x40FFFFFF);
				graphics.text(font, ellipsize(font, slot.label() + ": empty", labelMaxWidth),
						labelX, iconY + 1, COLOUR_EMPTY);
			} else {
				graphics.item(stack, iconX, iconY);
				graphics.itemDecorations(font, stack, iconX, iconY);

				graphics.text(font, ellipsize(font, stack.getHoverName().getString(), labelMaxWidth),
						labelX, iconY + 1, COLOUR_LABEL);

				String durability;
				int durabilityColour;
				if (stack.isDamageableItem()) {
					int max = stack.getMaxDamage();
					int remaining = max - stack.getDamageValue();
					durability = remaining + " / " + max;
					durabilityColour = durabilityColour(remaining, max);
				} else {
					durability = "\u2014";
					durabilityColour = COLOUR_LABEL;
				}
				graphics.text(font, Component.literal(durability),
						labelX, iconY + 1 + font.lineHeight + 1, durabilityColour);
			}

			y += ROW_HEIGHT;
		}

		Component total = Component.literal("Total armour points: " + player.getArmorValue());
		graphics.text(font, total, textX, y, COLOUR_TITLE);
	}

	private static Component ellipsize(Font font, String text, int maxWidth) {
		if (maxWidth <= 0 || font.width(text) <= maxWidth) {
			return Component.literal(text);
		}

		final String ellipsis = "...";
		int ellipsisWidth = font.width(ellipsis);
		if (ellipsisWidth >= maxWidth) {
			return Component.literal(font.plainSubstrByWidth(ellipsis, maxWidth));
		}

		return Component.literal(font.plainSubstrByWidth(text, maxWidth - ellipsisWidth) + ellipsis);
	}

	private static int durabilityColour(int remaining, int max) {
		if (max <= 0) {
			return COLOUR_LABEL;
		}
		float ratio = (float) remaining / (float) max;
		if (ratio > 0.5f) {
			return COLOUR_GOOD;
		}
		if (ratio > 0.25f) {
			return COLOUR_WARN;
		}
		return COLOUR_BAD;
	}

	private record SlotLabel(EquipmentSlot slot, String label) {
	}
}
