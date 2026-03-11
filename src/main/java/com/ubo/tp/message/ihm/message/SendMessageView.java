package main.java.com.ubo.tp.message.ihm.message;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import main.java.com.ubo.tp.message.common.EmojiImageUtil;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Composant graphique de saisie d'un message avec support d'image.
 */
public class SendMessageView extends BorderPane {

	private TextArea mTextArea;
	private Button mSendButton;
	private Button mImageButton;
	private Label mImagePreviewLabel;
	private String mSelectedImageBase64 = null;
	private List<User> mAvailableUsers = new ArrayList<>();
	private ContextMenu mMentionMenu = new ContextMenu();
	private ContextMenu mEmojiMenu = new ContextMenu();
	private Popup mEmojiPickerPopup = null;

	private EventHandler<ActionEvent> mSendListener;

	private static final String[][] EMOJI_LIST = {
		{":smile:", "😊"}, {":smirk:", "😏"}, {":sad:", "😢"}, {":laugh:", "😂"},
		{":love:", "❤️"}, {":thumbsup:", "👍"}, {":fire:", "🔥"}, {":party:", "🎉"},
		{":wink:", "😉"}, {":cool:", "😎"}, {":angry:", "😠"}, {":wow:", "😮"},
		{":cry:", "😭"}, {":star:", "⭐"}, {":heart:", "💖"}, {":sunglasses:", "🕶️"}
	};

	public SendMessageView() {
		this.setPadding(new Insets(8, 10, 8, 10));
		this.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

		mTextArea = new TextArea();
		mTextArea.setPrefRowCount(3);
		mTextArea.setPrefColumnCount(30);
		mTextArea.setWrapText(true);
		mTextArea.setFont(Font.font("Arial", 13));
		mTextArea.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;");
		mTextArea.setPromptText("Message... (Entrée pour envoyer, Maj+Entrée pour retour à la ligne)");
		mTextArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isShiftDown()) {
				event.consume();
				if (mSendListener != null) mSendListener.handle(new javafx.event.ActionEvent());
			}
		});

		Label charCounter = new Label("0/200");
		charCounter.setFont(Font.font("Arial", 10));
		charCounter.setStyle("-fx-text-fill: #95A5A6;");
		mTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.length() > 200) {
				mTextArea.setText(newVal.substring(0, 200));
				return;
			}
			charCounter.setText(newVal.length() + "/200");
			charCounter.setStyle(newVal.length() >= 180
				? "-fx-text-fill: #E74C3C;"
				: "-fx-text-fill: #95A5A6;");
		});

		mImageButton = new Button("📎");
		mImageButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand; -fx-font-size: 16; -fx-padding: 4 6;");
		mImageButton.setOnAction(e -> handleSelectImage());

		mSendButton = new Button("Envoyer");
		mSendButton.setPrefWidth(100);
		mSendButton.setMaxHeight(Double.MAX_VALUE);
		mSendButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13;");

		// Ligne de prévisualisation de l'image sélectionnée
		mImagePreviewLabel = new Label();
		mImagePreviewLabel.setStyle("-fx-text-fill: #1DA1F2; -fx-font-size: 11;");
		mImagePreviewLabel.setVisible(false);

		// Détecter @ (mention) et : (emoji) pour l'autocomplétion
		mTextArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
			handleMentionDetection(newPos.intValue());
			handleEmojiDetection(newPos.intValue());
		});

		VBox leftBox = new VBox(4, mTextArea, charCounter, mImagePreviewLabel);
		VBox.setVgrow(mTextArea, javafx.scene.layout.Priority.ALWAYS);

		Button emojiPickerBtn = new Button("😊");
		emojiPickerBtn.setStyle("-fx-font-size: 16; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 6;");
		emojiPickerBtn.setOnAction(e -> {
			if (mEmojiPickerPopup == null) mEmojiPickerPopup = buildEmojiPicker();
			if (mEmojiPickerPopup.isShowing()) {
				mEmojiPickerPopup.hide();
			} else {
				Bounds b = emojiPickerBtn.localToScreen(emojiPickerBtn.getBoundsInLocal());
				mEmojiPickerPopup.show(emojiPickerBtn, b.getMinX(), b.getMinY() - 260);
			}
		});

		VBox buttonBox = new VBox(5, emojiPickerBtn, mImageButton, mSendButton);
		buttonBox.setMaxHeight(Double.MAX_VALUE);

		HBox hbox = new HBox(5, leftBox, buttonBox);
		HBox.setHgrow(leftBox, Priority.ALWAYS);
		this.setCenter(hbox);
	}

	private void handleSelectImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Sélectionner une image");
		fileChooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
		);

		File file = fileChooser.showOpenDialog(this.getScene().getWindow());
		if (file != null) {
			try {
				// Lire et redimensionner l'image (max 600px de large)
				BufferedImage original = ImageIO.read(file);
				BufferedImage resized = resizeImage(original, 600);

				// Encoder en base64
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(resized, "png", baos);
				mSelectedImageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

				// Afficher le nom du fichier sélectionné
				mImagePreviewLabel.setText("📎 " + file.getName());
				mImagePreviewLabel.setVisible(true);

			} catch (Exception ex) {
				mImagePreviewLabel.setText("Erreur de chargement de l'image.");
				mImagePreviewLabel.setVisible(true);
			}
		}
	}

	private BufferedImage resizeImage(BufferedImage original, int maxWidth) {
		if (original.getWidth() <= maxWidth) {
			return original;
		}
		int newHeight = (int) ((double) original.getHeight() / original.getWidth() * maxWidth);
		java.awt.Image scaled = original.getScaledInstance(maxWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
		BufferedImage result = new BufferedImage(maxWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		result.getGraphics().drawImage(scaled, 0, 0, null);
		return result;
	}

	public void addSendListener(EventHandler<ActionEvent> listener) {
		mSendButton.setOnAction(listener);
		this.mSendListener = listener;
	}

	public String getText() {
		return mTextArea.getText().trim();
	}

	public String getSelectedImageBase64() {
		return mSelectedImageBase64;
	}

	public void clearText() {
		mTextArea.setText("");
		mSelectedImageBase64 = null;
		mImagePreviewLabel.setVisible(false);
	}

	public void setAvailableUsers(Set<User> users) {
		mAvailableUsers = users.stream()
			.filter(u -> !u.isDeleted())
			.collect(Collectors.toList());
	}

	private void handleMentionDetection(int caretPos) {
		String text = mTextArea.getText();
		if (caretPos <= 0 || caretPos > text.length()) {
			mMentionMenu.hide();
			return;
		}
		String textBeforeCaret = text.substring(0, caretPos);
		int atIndex = -1;
		for (int i = textBeforeCaret.length() - 1; i >= 0; i--) {
			char c = textBeforeCaret.charAt(i);
			if (c == '@') { atIndex = i; break; }
			if (c == ' ' || c == '\n') break;
		}
		if (atIndex < 0) { mMentionMenu.hide(); return; }

		String prefix = textBeforeCaret.substring(atIndex + 1);
		List<User> matches = mAvailableUsers.stream()
			.filter(u -> u.getUserTag().toLowerCase().startsWith(prefix.toLowerCase()))
			.sorted(Comparator.comparing(User::getUserTag))
			.limit(6)
			.collect(Collectors.toList());

		if (matches.isEmpty()) { mMentionMenu.hide(); return; }

		final int finalAtIndex = atIndex;
		final int finalCaretPos = caretPos;
		mMentionMenu.getItems().clear();
		for (User user : matches) {
			MenuItem item = new MenuItem("@" + user.getUserTag() + "  (" + user.getName() + ")");
			item.setOnAction(e -> insertMention(user.getUserTag(), finalAtIndex, finalCaretPos));
			mMentionMenu.getItems().add(item);
		}
		Bounds bounds = mTextArea.localToScreen(mTextArea.getBoundsInLocal());
		if (bounds != null) {
			mMentionMenu.show(mTextArea, bounds.getMinX() + 10, bounds.getMaxY() - 20);
		}
	}

	private void handleEmojiDetection(int caretPos) {
		String text = mTextArea.getText();
		if (caretPos <= 0 || caretPos > text.length()) { mEmojiMenu.hide(); return; }
		String textBeforeCaret = text.substring(0, caretPos);
		int colonIndex = -1;
		for (int i = textBeforeCaret.length() - 1; i >= 0; i--) {
			char c = textBeforeCaret.charAt(i);
			if (c == ':') { colonIndex = i; break; }
			if (c == ' ' || c == '\n') break;
		}
		if (colonIndex < 0) { mEmojiMenu.hide(); return; }
		String prefix = ":" + textBeforeCaret.substring(colonIndex + 1);
		List<String[]> matches = new ArrayList<>();
		for (String[] e : EMOJI_LIST) {
			if (e[0].toLowerCase().startsWith(prefix.toLowerCase())) matches.add(e);
			if (matches.size() >= 8) break;
		}
		if (matches.isEmpty()) { mEmojiMenu.hide(); return; }
		final int finalColonIndex = colonIndex;
		final int finalCaretPos = caretPos;
		mEmojiMenu.getItems().clear();
		for (String[] e : matches) {
			MenuItem item = new MenuItem(e[1] + "  " + e[0]);
			item.setOnAction(ev -> insertEmoji(e[1], finalColonIndex, finalCaretPos));
			mEmojiMenu.getItems().add(item);
		}
		Bounds bounds = mTextArea.localToScreen(mTextArea.getBoundsInLocal());
		if (bounds != null) mEmojiMenu.show(mTextArea, bounds.getMinX() + 10, bounds.getMaxY() - 20);
	}

	private void insertEmoji(String emoji, int colonIndex, int caretPos) {
		String text = mTextArea.getText();
		String before = text.substring(0, colonIndex);
		String after = caretPos < text.length() ? text.substring(caretPos) : "";
		String newText = before + emoji + " " + after;
		if (newText.length() <= 200) {
			mTextArea.setText(newText);
			mTextArea.positionCaret(colonIndex + emoji.length() + 1);
		}
		mEmojiMenu.hide();
	}

	/** Insère un emoji à la position du curseur */
	private void insertAtCaret(String text) {
		int caret = mTextArea.getCaretPosition();
		String current = mTextArea.getText();
		String newText = current.substring(0, caret) + text + current.substring(caret);
		if (newText.length() <= 200) {
			mTextArea.setText(newText);
			mTextArea.positionCaret(caret + text.length());
		}
		mTextArea.requestFocus();
	}

	/** Construit le popup picker d'emojis avec catégories */
	private Popup buildEmojiPicker() {
		Popup popup = new Popup();
		popup.setAutoHide(true);

		String[][][] categories = {
			{ // Émotions
				{"😊","😏","😢","😂","😮","😉","😎","😠","😭","🤔","😘","🥰","😜","🤗","😴","🤧"},
			},
			{ // Gestes
				{"👍","👎","👋","🤝","✌️","🤞","👊","🙌","💪","🖐️","👏","🤜","🤛","✋","🤙","👌"},
			},
			{ // Symboles & fête
				{"❤️","🔥","🎉","⭐","💖","🎊","✨","🎈","🌟","💥","🎀","🌈","💯","🏆","🎯","💫"},
			},
		};
		String[] catLabels = {"😊  Émotions", "👍  Gestes", "❤️  Symboles"};

		VBox content = new VBox(6);
		content.setStyle(
			"-fx-background-color: white;" +
			"-fx-border-color: #E0E0E0; -fx-border-radius: 10;" +
			"-fx-background-radius: 10;" +
			"-fx-padding: 10;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 12, 0, 0, 3);"
		);
		content.setPrefWidth(300);

		for (int c = 0; c < categories.length; c++) {
			Label catLabel = new Label(catLabels[c]);
			catLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
			catLabel.setStyle("-fx-text-fill: #95A5A6;");
			content.getChildren().add(catLabel);

			FlowPane flow = new FlowPane(2, 2);
			for (String emoji : categories[c][0]) {
				Button btn = new Button(emoji);
				btn.setStyle("-fx-background-color: transparent; -fx-font-size: 20; -fx-cursor: hand; -fx-padding: 3 4;");
				btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #F0F4F8; -fx-background-radius: 6; -fx-font-size: 20; -fx-cursor: hand; -fx-padding: 3 4;"));
				btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color: transparent; -fx-font-size: 20; -fx-cursor: hand; -fx-padding: 3 4;"));
				btn.setOnAction(e -> { insertAtCaret(emoji); popup.hide(); });
				flow.getChildren().add(btn);
			}
			content.getChildren().add(flow);
		}

		ScrollPane scroll = new ScrollPane(content);
		scroll.setFitToWidth(true);
		scroll.setPrefHeight(260);
		scroll.setStyle("-fx-background: transparent; -fx-border-width: 0;");
		popup.getContent().add(scroll);
		return popup;
	}

	private void insertMention(String tag, int atIndex, int caretPos) {
		String text = mTextArea.getText();
		String before = text.substring(0, atIndex);
		String after = caretPos < text.length() ? text.substring(caretPos) : "";
		String mention = "@" + tag + " ";
		String newText = before + mention + after;
		if (newText.length() <= 200) {
			mTextArea.setText(newText);
			mTextArea.positionCaret(atIndex + mention.length());
		}
		mMentionMenu.hide();
	}
}
