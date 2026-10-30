package io.github.steveswinsburg.armourstats.client;

import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Draws equipped armour status on the HUD. Two layouts are available and can
 * be switched with {@code /armourstats}:
 * <ul>
 *   <li>{@link DisplayMode#PANEL} - detailed top-left panel</li>
 *   <li>{@link DisplayMode#HOTBAR} - icon row to the left of the hotbar</li>
 * </ul>
 */
public class ArmourStatsHud implements HudElement {
	private static final List<SlotLabel> SLOTS = List.of(
			new SlotLabel(EquipmentSlot.HEAD, "Head"),
			new SlotLabel(EquipmentSlot.CHEST, "Chest"),
			new SlotLabel(EquipmentSlot.LEGS, "Legs"),
			new SlotLabel(EquipmentSlot.FEET, "Feet"));

	private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");

	private static final int MARGIN = 4;
	private static final int PADDING = 4;
	private static final int ICON = 16;
	private static final int ROW_HEIGHT = 20;
	private static final int TEXT_GAP = 4;
	private static final int PANEL_WIDTH = 150;

	// pixel size of one vanilla hotbar square (and spacing between items)
	private static final int HOTBAR_SLOT = 20;
	
	// height of the vanilla hotbar background sprite
	private static final int HOTBAR_HEIGHT = 22;
	
	// half-width of the vanilla hotbar background (182 / 2, rounded)
	private static final int HOTBAR_HALF_WIDTH = 91;
	
	// gap between the armour row and the hotbar (or offhand slot)
	private static final int HOTBAR_GAP = 4;
	
	// vanilla hotbar texture size used when cropping the first four slots
	private static final int HOTBAR_TEXTURE_WIDTH = 182;
	private static final int HOTBAR_TEXTURE_HEIGHT = 22;

	// Left border + four 20px slots (without the trailing mid-hotbar divider).
	private static final int HOTBAR_ROW_BODY_WIDTH = 1 + HOTBAR_SLOT * SLOTS.size();
	// Outer right border taken from the far end of the hotbar texture.
	private static final int HOTBAR_ROW_BORDER_WIDTH = 1;
	// Total drawn width: left border + 4 slots + right border.
	private static final int HOTBAR_ROW_WIDTH = HOTBAR_ROW_BODY_WIDTH + HOTBAR_ROW_BORDER_WIDTH;
	
	// width of the vanilla offhand slot sprite
	private static final int OFFHAND_SLOT_WIDTH = 29;

	private static final int COLOUR_BACKGROUND = 0x90000000;
	private static final int COLOUR_TITLE = 0xFFFFD700;
	private static final int COLOUR_LABEL = 0xFFFFFFFF;
	private static final int COLOUR_EMPTY = 0xFF808080;
	private static final int COLOUR_GOOD = 0xFF55FF55;
	private static final int COLOUR_WARN = 0xFFFFFF55;
	private static final int COLOUR_BAD = 0xFFFF5555;

	// render the hud
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		
		// HudElementRegistry.addLast does not inherit vanilla HUD visibility, so also honour F1 (Hud.isHidden) so this panel disappears with the rest of the HUD.
		if (player == null || minecraft.gui.hud.isHidden()) {
			return;
		}

		Font font = minecraft.font;
		if (ArmourStatsConfig.getDisplayMode() == DisplayMode.HOTBAR) {
			renderHotbarRow(graphics, font, player);
		} else {
			renderPanel(graphics, font, player);
		}
	}

	// render the top left panel
	private static void renderPanel(GuiGraphicsExtractor graphics, Font font, LocalPlayer player) {
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

	// render the hotbar icon row
	private static void renderHotbarRow(GuiGraphicsExtractor graphics, Font font, LocalPlayer player) {
		int guiHeight = graphics.guiHeight();
		int centerX = graphics.guiWidth() / 2;
		int hotbarLeft = centerX - HOTBAR_HALF_WIDTH;

		// Always reserve the left offhand slot for right-handed players so the
		// armour row does not jump or overlap when the offhand is equipped.
		int anchorRight = hotbarLeft;
		if (player.getMainArm().getOpposite() == HumanoidArm.LEFT) {
			anchorRight = hotbarLeft - OFFHAND_SLOT_WIDTH;
		}

		int left = Math.max(MARGIN, anchorRight - HOTBAR_GAP - HOTBAR_ROW_WIDTH);
		int top = guiHeight - HOTBAR_HEIGHT;

		// Cropping only the first four slots leaves a slot divider on the right.
		// Draw that body, then append the hotbar's real outer right border.
		graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				HOTBAR_SPRITE,
				HOTBAR_TEXTURE_WIDTH,
				HOTBAR_TEXTURE_HEIGHT,
				0,
				0,
				left,
				top,
				HOTBAR_ROW_BODY_WIDTH,
				HOTBAR_HEIGHT);
		graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				HOTBAR_SPRITE,
				HOTBAR_TEXTURE_WIDTH,
				HOTBAR_TEXTURE_HEIGHT,
				HOTBAR_TEXTURE_WIDTH - HOTBAR_ROW_BORDER_WIDTH,
				0,
				left + HOTBAR_ROW_BODY_WIDTH,
				top,
				HOTBAR_ROW_BORDER_WIDTH,
				HOTBAR_HEIGHT);

		int seed = 1;
		for (int i = 0; i < SLOTS.size(); i++) {
			ItemStack stack = player.getItemBySlot(SLOTS.get(i).slot());
			if (stack.isEmpty()) {
				continue;
			}

			// Match vanilla hotbar item inset: +3 from the slot cell, y = height - 19.
			int iconX = left + 3 + (i * HOTBAR_SLOT);
			int iconY = guiHeight - 19;
			graphics.item(player, stack, iconX, iconY, seed++);
			graphics.itemDecorations(font, stack, iconX, iconY);
		}
	}

	// ellipsis the text if it is too long
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

	// colour the durability based on the remaining durability
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
