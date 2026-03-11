package main.java.com.ubo.tp.message.common;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Utilitaire pour rendre des emoji en images JavaFX colorées via AWT.
 * Contourne la limitation de JavaFX qui affiche les emoji en noir et blanc.
 */
public class EmojiImageUtil {

	/** Cache pour éviter de re-rendre le même emoji */
	private static final Map<String, Image> CACHE = new HashMap<>();

	/** Police emoji selon la plateforme */
	private static final String EMOJI_FONT_NAME;

	static {
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("mac")) {
			EMOJI_FONT_NAME = "Apple Color Emoji";
		} else if (os.contains("win")) {
			EMOJI_FONT_NAME = "Segoe UI Emoji";
		} else {
			EMOJI_FONT_NAME = "Noto Color Emoji";
		}
	}

	/**
	 * Convertit un emoji en Image JavaFX colorée.
	 * @param emoji  le caractère emoji (ex: "😊")
	 * @param fontSize taille en px
	 * @return Image colorée, ou null en cas d'erreur
	 */
	public static Image toImage(String emoji, int fontSize) {
		String key = emoji + "_" + fontSize;
		return CACHE.computeIfAbsent(key, k -> renderEmoji(emoji, fontSize));
	}

	private static Image renderEmoji(String emoji, int fontSize) {
		try {
			Font font = new Font(EMOJI_FONT_NAME, Font.PLAIN, fontSize);
			int size = (int)(fontSize * 1.6) + 4;

			BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setFont(font);

			FontMetrics fm = g.getFontMetrics();
			int x = Math.max(0, (size - fm.stringWidth(emoji)) / 2);
			int y = (size - fm.getHeight()) / 2 + fm.getAscent();
			g.drawString(emoji, x, y);
			g.dispose();

			return SwingFXUtils.toFXImage(img, null);
		} catch (Exception e) {
			return null;
		}
	}
}
